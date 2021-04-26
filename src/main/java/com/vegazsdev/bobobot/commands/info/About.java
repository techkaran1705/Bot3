package com.vegazsdev.bobobot.commands.info;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class About extends Command {

    public About() {
        super("about", "About bot");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(
                prefs.getString("about_bot")
                        .replace("%1", bot.getVersionID())
        );
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.enableMarkdown(true);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton RIL = new InlineKeyboardButton();
        RIL.setText(prefs.getString("about_sourcode"));
        RIL.setUrl("https://github.com/TrebleExperience/Bot3");
        rowInline.add(RIL);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton RIL2 = new InlineKeyboardButton();
        RIL2.setText(prefs.getString("about_treble_channel"));
        RIL2.setUrl("https://t.me/trebleexperience");
        rowInline2.add(RIL2);

        rowsInline.add(rowInline);
        rowsInline.add(rowInline2);
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        bot.sendMessageSync(sendMessage);
    }
}