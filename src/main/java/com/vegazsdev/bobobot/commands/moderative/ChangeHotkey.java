package com.vegazsdev.bobobot.commands.moderative;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.XMLs;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Objects;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class ChangeHotkey extends Command {

    private final String supportedHotkeys = XMLs.getFromStringsXML("core-strings.xml", "possible_hotkeys");

    public ChangeHotkey() {
        super("chkey", "Change current hotkey on this chat");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (update.getMessage().getText().contains(" ")) {
            if (bot.isUserAdminOrPV(update)) {
                if (update.getMessage().getText().trim().equals(prefs.getHotkey() + "chkey".trim())) {
                    bot.sendMessage(prefs.getString("chkey_help")
                            .replace("%1", prefs.getHotkey())
                            .replace("%2", Objects.requireNonNull(supportedHotkeys)), update);
                } else {
                    if (update.getMessage().getText().contains(" ")) {
                        String msg = update.getMessage().getText().trim().split(" ")[1];
                        if (Objects.requireNonNull(supportedHotkeys).contains(msg)) {
                            DbThings.changeHotkey(prefs.getId(), msg);
                            prefs = DbThings.selectIntoPrefsTable(prefs.getId());
                            bot.sendMessage(prefs.getString("chkey_cur_hotkey")
                                    .replace("%1", prefs.getHotkey()), update);
                        } else {
                            bot.sendMessage(prefs.getString("chkey_error"), update);
                        }
                    } else {
                        bot.sendMessage(prefs.getString("something_went_wrong"), update);
                    }
                }
            }
        } else {
            bot.sendMessage(prefs.getString("bad_usage"), update);
        }
    }
}