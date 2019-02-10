package com.bradhenry.DiscordVoiceRecorder;

import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class AudioReceiveHandlerImpl implements AudioReceiveHandler {

    private static final Log LOG = LogFactory.getLog("AudioReceiveHandlerImpl");

    private final OutputStream outputStream;
    private final File file;
    private boolean canReceive = true;

    public AudioReceiveHandlerImpl(File file) throws IOException {
        this.file = file;
        this.outputStream = new FileOutputStream(file);
    }

    @Override
    public boolean canReceiveCombined() {
        return canReceive;
    }

    @Override
    public boolean canReceiveUser() {
        return false;
    }

    @Override
    public void handleCombinedAudio(CombinedAudio combinedAudio) {
        byte[] audioData = combinedAudio.getAudioData(1.0);
        try {
            outputStream.write(audioData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleUserAudio(UserAudio userAudio) {
    }

    public void shutdown() {
        canReceive = false;
        try {
            outputStream.flush();
            outputStream.close();
            String absolutePath = this.file.getAbsolutePath();
            String outputPath = absolutePath.substring(0, absolutePath.length() - 3) + "mp3";

            FFmpegBuilder builder = new FFmpegBuilder();
            builder.setFormat("s16be").addExtraArgs("-ac", "2", "-ar", "48000").setInput(absolutePath).addOutput(outputPath).done();
            FFmpegExecutor executor = new FFmpegExecutor();
            FFmpegJob job = executor.createJob(builder);
            job.run();
            if (job.getState() == FFmpegJob.State.FINISHED) {
                Files.delete(this.file.toPath());
            } else {
                LOG.warn("FFMPEG conversion did not complete successfully");
            }
        } catch (IOException e) {
            LOG.error("Error shutting down recording", e);
        }
    }

}
