package com.vegazsdev.bobobot.core.command;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * That class serves as a basis for command.
 */
public abstract class Command {
    private final String alias;

    public Command(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public abstract void botReply(Update update, TelegramBot bot, PrefObj prefs);
}