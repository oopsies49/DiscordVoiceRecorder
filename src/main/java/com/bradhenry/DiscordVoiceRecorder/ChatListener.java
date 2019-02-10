package com.bradhenry.DiscordVoiceRecorder;

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
import java.util.stream.Stream;

public class ChatListener extends ListenerAdapter {
    private final DiscordVoiceRecorderProperties properties;
    private static final Log LOG = LogFactory.getLog("ChatListener");

    public ChatListener(DiscordVoiceRecorderProperties properties) {
        this.properties = properties;
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

        try {
            Stream<Path> list = Files.list(new File(properties.getRecordingPath()).toPath());
            list.filter(path -> path.toString().endsWith(".mp3"))
                    .map(Path::toFile)
                    .max(Comparator.comparing(File::lastModified))
                    .ifPresent(file -> textChannel.sendFile(file, file.getName()).submit());
        } catch (IOException e) {
            LOG.error("Uploading recording failed", e);
        }
    }

    private void endRecordCommand(MessageReceivedEvent event) {
        if (event.getMember() == null || event.getMember().getGuild() == null) {
            return;
        }

        AudioManager audioManager = event.getMember().getGuild().getAudioManager();
        AudioReceiveHandler receiveHandler = audioManager.getReceiveHandler();
        if (receiveHandler instanceof AudioReceiveHandlerImpl) {
            ((AudioReceiveHandlerImpl) receiveHandler).shutdown();
        }

        audioManager.setSendingHandler(null);
        audioManager.setReceivingHandler(null);
        audioManager.closeAudioConnection();
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
            audioManager.setReceivingHandler(new AudioReceiveHandlerImpl(recordingFile));
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
