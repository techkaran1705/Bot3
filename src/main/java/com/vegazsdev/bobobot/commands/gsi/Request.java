package com.vegazsdev.bobobot.commands.gsi;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class Request extends Command {

    private String dontHaveUsername;

    public Request() {
        super("request", "Request a GSI with this command");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (Config.getDefConfig("privateChat") == null || Objects.equals(Config.getDefConfig("privateChat"), "")) {
            bot.sendReply(prefs.getString("issue_with_privatechat"), update);
        } else if (!(Config.getDefConfig("requestChat") == null) || !Objects.equals(Config.getDefConfig("requestChat"), "")) {
            if (Objects.requireNonNull(Config.getDefConfig("requestChat")).startsWith("-") && Objects.requireNonNull(Config.getDefConfig("privateChat")).startsWith("-")) {
                // General base: message/id and some tricks
                String chatId = update.getMessage().getChatId().toString();
                String[] msgComparableRaw = update.getMessage().getText().split(" ");
                String msgSwitchPrefix = msgComparableRaw[0];
                String msgBaseRaw = update.getMessage().getText();

                // Regex for valid link
                String validLink = "(http:\\/\\/|https:\\/\\/)?[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(\\/.*)?";

                // General base: SendMessage() and switch() things
                SendMessage message = new SendMessage();
                message.setDisableWebPagePreview(true);
                message.enableMarkdown(true);
                message.setChatId(chatId);

                if (!(update.getMessage().getFrom().getId() == Float.parseFloat(String.valueOf(777000)))) {
                    if (update.getMessage().getChatId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("requestChat")))) {
                        if (Pattern.matches(validLink, msgComparableRaw[1])) {
                            // Delete the message who user sent
                            bot.deleteMessage(chatId, update.getMessage().getMessageId());

                            // Set to thanks message
                            message.setText(prefs.getString("request_done")
                                    .replace("%1", update.getMessage().getFrom().getFirstName())
                                    .replace("%2", String.valueOf(update.getMessage().getFrom().getId()))
                                    .replace("%3", String.valueOf(update.getMessage().getFrom().getId()))
                            );
                            message.setChatId(chatId); // Get to stock chat id
                            bot.sendMessageSync(message);

                            // Set dontHaveUsername
                            dontHaveUsername = prefs.getString("dont_have");

                            // workaround to info
                            StringBuilder str = new StringBuilder();
                            String addInfo = "";
                            for (int i = 2; i < msgComparableRaw.length; i++) {
                                str.append(msgComparableRaw[i]).append(" ");
                                addInfo = String.valueOf(str);
                            }
                            if (addInfo.equals("")) addInfo = prefs.getString("info_not_shared");

                            /*
                             * Prepare InlineKeyboardButton
                             */
                            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                            List<List<InlineKeyboardButton>> inlineKeyboardButton = new ArrayList<>();

                            List<InlineKeyboardButton> inlineKeyboardButtonArrayList = new ArrayList<>();
                            InlineKeyboardButton inlineKeyboardButtonAonly = new InlineKeyboardButton();
                            inlineKeyboardButtonAonly.setText("\uD83D\uDCE6 Firmware/ROM Link");
                            inlineKeyboardButtonAonly.setUrl(msgComparableRaw[1]);
                            inlineKeyboardButtonArrayList.add(inlineKeyboardButtonAonly);
                            inlineKeyboardButton.add(inlineKeyboardButtonArrayList);

                            /*
                             * Finish InlineKeyboardButton setup
                             */
                            markupInline.setKeyboard(inlineKeyboardButton);
                            message.setReplyMarkup(markupInline);

                            // Initial to the message base
                            message.setChatId(Objects.requireNonNull(Config.getDefConfig("privateChat")));
                            message.setText(
                                    prefs.getString("gsi_order") + "\n\n"
                                            + prefs.getString("addinfo") + "\n"
                                            + "`" + addInfo + "`"
                                            + "\n\n"
                                            + prefs.getString("user_info") + "\n\n"
                                            + prefs.getString("first_and_last_name")
                                            .replace("%1", update.getMessage().getFrom().getFirstName() + validateLastName(update.getMessage().getFrom().getLastName())) + "\n"
                                            + prefs.getString("user_name")
                                            .replace("%1", validateUsername(update.getMessage().getFrom().getUserName())) + "\n"
                                            + prefs.getString("user_id")
                                            .replace("%1", String.valueOf(update.getMessage().getFrom().getId())) + "\n"
                                            + prefs.getString("lang_code")
                                            .replace("%1", update.getMessage().getFrom().getLanguageCode())
                            );
                        } else {
                            message.setText(prefs.getString("invalid_link")
                                    .replace("%1", update.getMessage().getFrom().getFirstName())
                                    .replace("%2", String.valueOf(update.getMessage().getFrom().getId()))
                                    .replace("%3", String.valueOf(update.getMessage().getFrom().getId()))
                            );

                            // Delete the message who user sent
                            bot.deleteMessage(chatId, update.getMessage().getMessageId());
                        }
                    } else {
                        message.setText(prefs.getString("cant_be_used"));
                    }

                    /*
                     * Send the message
                     */
                    bot.sendMessageSync(message);
                } else {
                    bot.deleteMessage(chatId, update.getMessage().getMessageId());
                }
            } else {
                bot.sendReply(prefs.getString("issue_with_index_chat"), update);
            }
        } else {
            bot.sendReply(prefs.getString("issue_with_publicchat"), update);
        }
    }

    private String validateUsername(String username) {
        if (username == null || username.equals("")) {
            return dontHaveUsername;
        } else {
            return "@" + username;
        }
    }

    private String validateLastName(String lastName) {
        if (lastName == null || lastName.equals("")) {
            return "";
        } else {
            return " " + lastName;
        }
    }
}