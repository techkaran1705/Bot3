package com.vegazsdev.bobobot.commands.moderative;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class DisableRandomMessage extends Command {

    public DisableRandomMessage() {
        super("rm", "Disable 'Random Messages' with this command");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        String[] msgComparableRaw = update.getMessage().getText().split(" ");

        if (bot.isPM(update.getMessage().getChatId().toString(), update.getMessage().getFrom().getId().toString())) {
            if (bot.isAdmin(update.getMessage().getFrom().getId().toString(), update.getMessage().getChatId().toString())) {
                if (update.getMessage().getText().contains(" ")) {
                    if (!Pattern.matches("[a-zA-Z|-]+", msgComparableRaw[1])) {
                        switch (Integer.parseInt(msgComparableRaw[1])) {
                            case 0 -> {
                                DbThings.changeAbleToSendRandomMessage(prefs.getId(), Double.parseDouble(msgComparableRaw[1]));
                                DbThings.selectIntoPrefsTable(prefs.getId());
                                bot.sendReply(prefs.getString("rm_done_0"), update);
                            }
                            case 1 -> {
                                DbThings.changeAbleToSendRandomMessage(prefs.getId(), Double.parseDouble(msgComparableRaw[1]));
                                DbThings.selectIntoPrefsTable(prefs.getId());
                                bot.sendReply(prefs.getString("rm_done_1"), update);
                            }
                            default -> bot.sendReply(prefs.getString("bad_usage"), update);
                        }
                    } else {
                        bot.sendReply(prefs.getString("bad_usage"), update);
                    }
                } else {
                    bot.sendReply(prefs.getString("bad_usage"), update);
                }
            } else {
                bot.sendReply(prefs.getString("only_admin_can_run"), update);
            }
        }
    }
}