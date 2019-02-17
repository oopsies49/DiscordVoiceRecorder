package com.bradhenry.discordvoicerecorder.service;

import com.bradhenry.discordvoicerecorder.DiscordVoiceRecorderProperties;
import com.bradhenry.discordvoicerecorder.listeners.ChatListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;

@Service
public class VoiceRecorderBot {

    @Autowired
    VoiceRecorderBot(DiscordVoiceRecorderProperties properties) {
        JDA bot;
        try {
            bot = new JDABuilder(AccountType.BOT)
                    .setAudioEnabled(true)
                    .setAutoReconnect(true)
                    .setToken(properties.getBotToken())
                    .build()
                    .awaitReady();
        } catch (InterruptedException | LoginException e) {
            throw new RuntimeException("Couldn't initialize bot, startup failed", e);
        }

        ChatListener chatListener = new ChatListener(properties);
        bot.addEventListener(chatListener);
    }

}
