package com.vegazsdev.bobobot.commands.fun;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

@SuppressWarnings("unused")
public class Hello extends Command {
    public Hello() {
        super("hello");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        bot.sendReply(prefs.getString("hello").replace("%1", update.getMessage().getText()), update);
    }
}