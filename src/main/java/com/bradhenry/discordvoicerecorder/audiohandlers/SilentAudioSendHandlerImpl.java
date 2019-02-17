package com.bradhenry.discordvoicerecorder.audiohandlers;

import net.dv8tion.jda.core.audio.AudioSendHandler;

public class SilentAudioSendHandlerImpl implements AudioSendHandler {
    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public byte[] provide20MsAudio() {
        return new byte[]{(byte) 0xF8, (byte) 0xFF, (byte) 0xFE}; // Opus silence
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
