package com.vegazsdev.bobobot.commands.moderative;

import com.vegazsdev.bobobot.Main;
import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.XMLs;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * That class change language of chat.
 */
@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class ChangeLang extends Command {

    public ChangeLang() {
        super("chlang");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (update.getMessage().getText().contains(" ")) {
            if (bot.isPM(update.getMessage().getChatId().toString(), update.getMessage().getFrom().getId().toString())) {
                if (bot.isAdmin(update.getMessage().getFrom().getId().toString(), update.getMessage().getChatId().toString())) {
                    if (update.getMessage().getText().equals(prefs.getHotkey() + "chlang".trim())) {
                        bot.sendReply(prefs.getString("available_lang") + "\n" + XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "disp_lang"), update);
                    } else {
                        String msg = update.getMessage().getText().split(" ")[1].trim();
                        if (msg.contains(" ")) {
                            msg = msg.replace(" ", "");
                        }
                        if (msg.length() < 3) {
                            String hello = XMLs.getFromStringsXML("strings-" + msg + ".xml", "hello");
                            if (hello == null) {
                                bot.sendReply(prefs.getString("unknown_lang").replace("%1", prefs.getHotkey()), update);
                            } else {
                                DbThings.changeLanguage(prefs.getId(), "strings-" + msg + ".xml");
                                prefs = DbThings.selectIntoPrefsTable(prefs.getId());
                                bot.sendReply(prefs.getString("lang_updated"), update);
                            }
                        } else {
                            bot.sendReply(prefs.getString("unknown_lang").replace("%1", prefs.getHotkey()), update);
                        }
                    }
                } else {
                    bot.sendReply(prefs.getString("only_admin_can_run"), update);
                }
            }
        } else {
            bot.sendReply(prefs.getString("bad_usage"), update);
        }
    }
}