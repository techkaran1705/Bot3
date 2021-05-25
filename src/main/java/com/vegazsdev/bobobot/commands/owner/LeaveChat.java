package com.vegazsdev.bobobot.commands.owner;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

/**
 * Make the bot leave any chat you want with one command.
 */
@SuppressWarnings("unused")
public class LeaveChat extends Command {

    public LeaveChat() {
        super("leave");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        String[] msgComparableRaw = update.getMessage().getText().split(" ");
        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            if (update.getMessage().getText().contains(" ")) {
                if (bot.leaveChat(msgComparableRaw[1])) {
                    bot.sendReply(prefs.getString("done_i_left"), update);
                } else {
                    bot.sendReply(prefs.getString("something_went_wrong"), update);
                }
            } else {
                bot.sendReply(prefs.getString("bad_usage"), update);
            }
        }
    }
}