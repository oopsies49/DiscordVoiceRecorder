package com.bradhenry.DiscordVoiceRecorder;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ChatListener extends ListenerAdapter {
    private final DiscordVoiceRecorderProperties properties;

    public ChatListener(DiscordVoiceRecorderProperties properties, JDA bot) {
        this.properties = properties;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        if (message == null) {
            return;
        }

        if (!message.startsWith(properties.getCommandCharacter())) {
            return;
        }

        String lowerMessage = message.substring(properties.getCommandCharacter().length()).toLowerCase();
        if (lowerMessage.startsWith("startrecording")) {
            startRecordCommand(event);
        } else if (lowerMessage.startsWith("endrecording")) {
            endRecordCommand(event);
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

        String recordingFilePath = properties.getRecordingFilePath() + System.currentTimeMillis() + ".pcm.gz";
        File recordingFile = new File(recordingFilePath);
        try {
            if (!recordingFile.createNewFile()) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            GZIPOutputStream outputStream = new GZIPOutputStream(new FileOutputStream(recordingFile));
            audioManager.setReceivingHandler(new AudioReceiveHandlerImpl(outputStream));
            audioManager.setSendingHandler(new SilentAudioSendHandlerImpl());
            audioManager.openAudioConnection(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean preventRecording(MessageReceivedEvent event) {
        if (event.getChannelType() != ChannelType.TEXT) { // should be publicly known that recording is starting/stopping
            return true;
        }

        return false;
    }

}
