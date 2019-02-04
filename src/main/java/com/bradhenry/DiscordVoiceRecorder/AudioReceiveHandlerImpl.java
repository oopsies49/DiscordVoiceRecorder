package com.bradhenry.DiscordVoiceRecorder;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;

public class AudioReceiveHandlerImpl implements AudioReceiveHandler {

    private static final Log LOG = LogFactory.getLog("AudioReceiveHandlerImpl");

    private final OutputStream outputStream;

    public AudioReceiveHandlerImpl(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public boolean canReceiveCombined() {
        return true;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        byte[] audioData = combinedAudio.getAudioData(1.0);
        try {
            LOG.trace("received combined audio");
            boolean allnull = true;
            for (byte audioDatum : audioData) { // TODO WHY ALL NULL??
                if (audioDatum != 0) {
                    allnull = false;
                }
            }
            if (!allnull) {
                LOG.info("received combined audio data");
            }

            outputStream.write(audioData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
        byte[] audioData = userAudio.getAudioData(1.0);
        try {
            LOG.info("received user audio");
            outputStream.write(audioData);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void shutdown() {
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
