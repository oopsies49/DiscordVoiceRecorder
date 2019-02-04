package com.bradhenry.DiscordVoiceRecorder;

import net.dv8tion.jda.core.audio.AudioSendHandler;

class SilentAudioSendHandlerImpl implements AudioSendHandler {
    @Override
    public boolean canProvide() {
        return true;
    }

    @Override
    public byte[] provide20MsAudio() {
        byte[] silence = {(byte) 0xF8, (byte) 0xFF, (byte) 0xFE};
        return silence;
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
