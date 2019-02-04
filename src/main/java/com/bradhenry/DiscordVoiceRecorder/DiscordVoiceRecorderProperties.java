package com.bradhenry.DiscordVoiceRecorder;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties()
@Validated
public class DiscordVoiceRecorderProperties {

    @NonNull
    private String botToken;
    @NonNull
    private String commandCharacter;
    @NonNull
    private String recordingFilePath;

    @NonNull
    public String getBotToken() {
        return botToken;
    }

    public void setBotToken(@NonNull String botToken) {
        this.botToken = botToken;
    }

    @NonNull
    public String getCommandCharacter() {
        return commandCharacter;
    }

    public void setCommandCharacter(@NonNull String commandCharacter) {
        this.commandCharacter = commandCharacter;
    }

    public String getRecordingFilePath() {
        return recordingFilePath;
    }

    public void setRecordingFilePath(String recordingFilePath) {
        this.recordingFilePath = recordingFilePath;
    }
}
