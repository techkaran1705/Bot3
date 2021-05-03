package com.vegazsdev.bobobot.commands.fun;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

@SuppressWarnings("unused")
public class Echo extends Command {
    public Echo() {
        super("echo", "Say anything");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        String[] msgComparableRaw = update.getMessage().getText().split(" ");
        if (update.getMessage().getText().contains(" ")) {
            bot.sendReply(update.getMessage().getText().substring(msgComparableRaw[0].length()), update);
        }
    }
}