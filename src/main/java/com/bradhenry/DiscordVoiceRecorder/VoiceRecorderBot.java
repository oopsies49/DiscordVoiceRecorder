package com.bradhenry.DiscordVoiceRecorder;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public class VoiceRecorderBot {

    private final DiscordVoiceRecorderProperties properties;
    private final JDA bot;

    @Autowired
    VoiceRecorderBot(DiscordVoiceRecorderProperties properties) {
        this.properties = properties;
        try {
            this.bot = new JDABuilder(AccountType.BOT)
                    .setAudioEnabled(true)
                    .setAutoReconnect(true)
                    .setToken(properties.getBotToken())
                    .build()
                    .awaitReady();
        } catch (InterruptedException | LoginException e) {
            throw new RuntimeException("Couldn't initialize bot, startup failed", e);
        }

        ChatListener chatListener = new ChatListener(this.properties, this.bot);
        this.bot.addEventListener(chatListener);
    }

}
