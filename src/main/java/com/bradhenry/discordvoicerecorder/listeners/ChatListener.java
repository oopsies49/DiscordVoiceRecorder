package com.bradhenry.discordvoicerecorder.listeners;

import com.bradhenry.discordvoicerecorder.DiscordVoiceRecorderProperties;
import com.bradhenry.discordvoicerecorder.audiohandlers.AudioReceiveHandlerImpl;
import com.bradhenry.discordvoicerecorder.audiohandlers.SilentAudioSendHandlerImpl;
import com.bradhenry.discordvoicerecorder.aws.S3Uploader;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ChatListener extends ListenerAdapter {
    private final DiscordVoiceRecorderProperties properties;
    private static final Log LOG = LogFactory.getLog("ChatListener");
    private final ExecutorService executorService;

    public ChatListener(DiscordVoiceRecorderProperties properties) {
        this(properties, Executors.newCachedThreadPool());
    }

    private ChatListener(DiscordVoiceRecorderProperties properties, ExecutorService executorService) {
        this.properties = properties;
        this.executorService = executorService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message == null || !message.startsWith(properties.getCommandCharacter())) {
            return;
        }

        String lowerMessage = message.substring(properties.getCommandCharacter().length()).toLowerCase();
        if (lowerMessage.startsWith("start")) {
            startRecordCommand(event);
        } else if (lowerMessage.startsWith("end")) {
            endRecordCommand(event);
        } else if (lowerMessage.startsWith("upload")) {
            uploadRecordingCommand(event);
        }
    }

    private void uploadRecordingCommand(MessageReceivedEvent event) {
        TextChannel textChannel = event.getTextChannel();
        if (textChannel == null) {
            return;
        }

        executorService.submit(() -> {
            Consumer<File> fileUploader;
            if (properties.isUseAWS()) {
                fileUploader = file -> {
                    S3Uploader s3Uploader = new S3Uploader(properties);
                    String url = s3Uploader.uploadFile(file);
                    textChannel.sendMessage(url).submit();
                };
            } else {
                fileUploader = file -> textChannel.sendFile(file, file.getName()).submit();
            }

            try {
                Stream<Path> list = Files.list(new File(properties.getRecordingPath()).toPath());
                list.filter(path -> path.toString().endsWith(properties.getRecordingFormat()))
                        .map(Path::toFile)
                        .max(Comparator.comparing(File::lastModified))
                        .ifPresent(fileUploader);
            } catch (IOException e) {
                LOG.error("Uploading recording failed", e);
            }
        });
    }

    private void endRecordCommand(MessageReceivedEvent event) {
        if (event.getMember() == null || event.getMember().getGuild() == null) {
            return;
        }

        executorService.submit(() -> {
            AudioManager audioManager = event.getMember().getGuild().getAudioManager();
            AudioReceiveHandler receiveHandler = audioManager.getReceiveHandler();
            if (receiveHandler instanceof AudioReceiveHandlerImpl) {
                ((AudioReceiveHandlerImpl) receiveHandler).shutdown();
            }

            audioManager.setSendingHandler(null);
            audioManager.setReceivingHandler(null);
            audioManager.closeAudioConnection();
        });
    }

    private void startRecordCommand(MessageReceivedEvent event) {
        if (preventRecording(event)) {
            return;
        }

        if (event.getGuild() == null || event.getMember() == null || event.getMember().getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null) {
            return;
        }
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        AudioManager audioManager = event.getGuild().getAudioManager();

        String recordingFilePath = properties.getRecordingPath() + System.currentTimeMillis() + ".raw";
        File recordingFile = new File(recordingFilePath);
        try {
            Files.createFile(recordingFile.toPath());
        } catch (IOException e) {
            LOG.error("Start recording failed, couldn't create new file", e);
            return;
        }

        try {
            audioManager.setReceivingHandler(new AudioReceiveHandlerImpl(properties, recordingFile));
            audioManager.setSendingHandler(new SilentAudioSendHandlerImpl());
            audioManager.openAudioConnection(channel);
        } catch (IOException e) {
            LOG.error("Start recording failed", e);
        }
    }

    private boolean preventRecording(MessageReceivedEvent event) {
        return event.getChannelType() != ChannelType.TEXT; // should be publicly known that the bot is recording
    }

}
