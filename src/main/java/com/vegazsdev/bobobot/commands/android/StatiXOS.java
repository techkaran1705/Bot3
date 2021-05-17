package com.vegazsdev.bobobot.commands.android;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.JSONs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class StatiXOS extends Command {

    private static final Logger logger = LoggerFactory.getLogger(StatiXOS.class);

    public StatiXOS() {
        super("sxos", "A command");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        /*
         * Main vars
         */
        String[] msgComparableRaw = update.getMessage().getText().split(" ");

        /*
         * Prepare sendMessage
         */
        SendMessage sendMessage = new SendMessage();
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.enableHtml(true);

        /*
         * Prepare InlineKeyboardButton
         */
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        if (update.getMessage().getText().contains(" ")) {
            /*
             * Try to run
             */
            try {
                /*
                 * Prepare HttpClient base
                 */
                String urlBase = "https://downloads.statixos.com/json/";
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlBase + msgComparableRaw[1].replace(".json", "") + ".json"))
                        .build();

                /*
                 * Run the request
                 */
                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            if (response.statusCode() == 200) {
                                /*
                                 * Prepare the variables
                                 */
                                Timestamp timestamp = new Timestamp(JSONs.getLongOfArrayFromJSONObject(String.valueOf(response.body()), "response", "datetime"));
                                String urlToDownload = JSONs.getValueOfArrayFromJSONObject(String.valueOf(response.body()), "response", "url");
                                String romType = JSONs.getValueOfArrayFromJSONObject(String.valueOf(response.body()), "response", "romtype");
                                String romName = JSONs.getValueOfArrayFromJSONObject(String.valueOf(response.body()), "response", "filename");
                                String romVersion = JSONs.getValueOfArrayFromJSONObject(String.valueOf(response.body()), "response", "version");
                                String ID = JSONs.getValueOfArrayFromJSONObject(String.valueOf(response.body()), "response", "id");

                                /*
                                 * Set download variable into Inline Keyboard Button
                                 */
                                List<InlineKeyboardButton> inlineKeyboardButtonArrayList = new ArrayList<>();
                                InlineKeyboardButton downloadROM = new InlineKeyboardButton();

                                downloadROM.setText("\uD83D\uDCE6 Download");
                                downloadROM.setUrl(urlToDownload);
                                inlineKeyboardButtonArrayList.add(downloadROM);
                                rowsInline.add(inlineKeyboardButtonArrayList);

                                /*
                                 * Finish
                                 */
                                markupInline.setKeyboard(rowsInline);
                                sendMessage.setReplyMarkup(markupInline);

                                /*
                                 * Set the text
                                 */
                                sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
                                sendMessage.setText(
                                        "✉️ <b>File name</b>\n<code>"
                                        + romName + "</code>\n\n"
                                        + "❓ <b>Status Type</b>\n<code>"
                                        + romType + "</code>\n\n"
                                        + "\uD83D\uDCCD <b>ID</b>\n<code>"
                                        + ID + "</code>\n\n"
                                        + "\uD83D\uDD30 <b>Version</b>\n<code>"
                                        + romVersion + "</code>"
                                );
                                bot.sendMessageAsyncBase(sendMessage, update);
                            } else {
                                bot.sendReply("Failed", update);
                            }
                            return response;
                        })
                        .thenApply(HttpResponse::body);
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            }
        } else {
            bot.sendReply(prefs.getString("bad_usage"), update);
        }
    }
}