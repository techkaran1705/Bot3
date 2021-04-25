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
                "***─ About***" + "\n"
                + "***Bo³+t*** is written in java and originally developed by [VegaZS](tg://user?id=705707638)" + "\n\n"
                + "***─ Developer/Maintainer of Bo³+t*** (Official/Unofficial)" + "\n"
                + "[VegaZS](https://github.com/VegaBobo) - Original dev" + "\n"
                + "[Velosh](https://github.com/Velosh) - Unofficial maintainer of ***Bo³+t*** for Treble Experience"
        );
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.enableMarkdown(true);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton RIL = new InlineKeyboardButton();
        RIL.setText("❕ Source Code");
        RIL.setUrl("https://github.com/VeloshGSIs/Bot3");
        rowInline.add(RIL);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton RIL2 = new InlineKeyboardButton();
        RIL2.setText("\uD83C\uDF10 Treble Channel");
        RIL2.setUrl("https://t.me/trebleexperience");
        rowInline2.add(RIL2);

        rowsInline.add(rowInline);
        rowsInline.add(rowInline2);
        markupInline.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markupInline);

        bot.sendMessageSync(sendMessage);
    }
}