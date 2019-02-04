package com.bradhenry.DiscordVoiceRecorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@EnableConfigurationProperties(DiscordVoiceRecorderProperties.class)
@Configuration
public class DiscordVoiceRecorderApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscordVoiceRecorderApplication.class, args);
	}

}

