package com.bradhenry.discordvoicerecorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DiscordVoiceRecorderProperties.class)
public class DiscordVoiceRecorderApplication {
	public DiscordVoiceRecorderApplication() {
	}

	public static void main(String[] args) {
		SpringApplication.run(DiscordVoiceRecorderApplication.class, args);
	}

}

