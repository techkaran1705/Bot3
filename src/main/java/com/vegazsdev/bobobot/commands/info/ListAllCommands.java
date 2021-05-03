package com.vegazsdev.bobobot.commands.info;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

@SuppressWarnings("unused")
public class ListAllCommands extends Command {

    public ListAllCommands() {
        super("comm", "Show all commands");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        String hotkey = prefs.getHotkey();
        StringBuilder allCommandsAsString = new StringBuilder();
        for (int i = 0; i < bot.getActiveCommandsAsCmdObject().size(); i++) {
            allCommandsAsString.append("<b>").append(hotkey).append(bot.getActiveCommandsAsCmdObject().get(i).getAlias()).append("</b>\n")
                    .append("<i>").append(bot.getActiveCommandsAsCmdObject().get(i).getCommandInfo()).append("</i>\n\n");
        }
        bot.sendReply(allCommandsAsString.toString(), update);
    }
}