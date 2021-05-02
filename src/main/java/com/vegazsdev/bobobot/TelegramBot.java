package com.vegazsdev.bobobot;

import com.vegazsdev.bobobot.core.bot.Bot;
import com.vegazsdev.bobobot.core.command.CommandWithClass;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.XMLs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("rawtypes")
public class TelegramBot extends TelegramLongPollingBot {

    /*
     * Don't be angry guys, just a joke
     */
    private final String[] messages = {
            "Go back to the kitchen", "Uninstall the telegram, no one will care",
            "Have you ever wondered why you were born? There is no reason", "Go sleep",
            "Why do you use this command? Well, nobody cares", "Life is random chaos, ordered by time",
            "They got lost again, bunch of idiots!", "Breaking the rules is worse than trash, but abandoning your friends is worse than that.",
            "A chance in a million is better than no chance!", "When you love, there is a risk of hating.",
            "Have you washed the dishes yet?", "You're cringe."
    };

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final Bot bot;
    private ArrayList<Class> commandClasses;

    TelegramBot(Bot bot, ArrayList<Class> commandClasses) {
        this.bot = bot;
        this.commandClasses = commandClasses;
    }

    public TelegramBot(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onUpdateReceived(Update update) {
        /*
         * Avoid hotkey problem
         */
        if (!FileTools.checkFileExistsCurPath("databases/prefs.db")) {
            DbThings.createNewDatabase("prefs.db");
            DbThings.createTable("prefs.db",
                    "CREATE TABLE IF NOT EXISTS chat_prefs ("
                            + "group_id real UNIQUE PRIMARY KEY,"
                            + "hotkey text DEFAULT '!',"
                            + "lang text DEFAULT 'strings-en.xml'"
                            + ");"
            );
        }

        new Thread(new Runnable() {
            private TelegramBot tBot;

            Runnable init(TelegramBot tBot) {
                this.tBot = tBot;
                return this;
            }

            @Override
            public void run() {
                if (update.hasMessage() && update.getMessage().getText() != null
                        && !update.getMessage().getText().equals("")
                        && Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "possible_hotkeys"))
                        .indexOf(update.getMessage().getText().charAt(0)) >= 0) {

                    boolean trueToRun;

                    String msg = update.getMessage().getText();
                    long usrId = update.getMessage().getFrom().getId();
                    PrefObj chatPrefs = getPrefs(update);

                    if (chatPrefs == null) {
                        chatPrefs = new PrefObj(0, "strings-en.xml", "!");
                        sendReply(XMLs.getFromStringsXML("core-strings.xml", "run_command_again"), update);
                        trueToRun = false;
                    } else {
                        trueToRun = true;
                    }

                    if (trueToRun) {
                        if (chatPrefs.getHotkey() != null && msg.startsWith(Objects.requireNonNull(chatPrefs.getHotkey()))) {
                            for (CommandWithClass commandWithClass : getActiveCommandsAsCmdObject()) {
                                String adjustCommand = msg.replace(Objects.requireNonNull(chatPrefs.getHotkey()), "");

                                if (adjustCommand.contains(" ")) {
                                    adjustCommand = adjustCommand.split(" ")[0];
                                }

                                if (commandWithClass.getAlias().equals(adjustCommand)) {
                                    try {
                                        runMethod(commandWithClass.getClazz(), update, tBot, chatPrefs);
                                        logger.info(Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "command_ok"))
                                                .replace("%1", update.getMessage().getFrom().getFirstName() + " (" + usrId + ")")
                                                .replace("%2", adjustCommand));
                                    } catch (Exception e) {
                                        logger.error(Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "command_failure"))
                                                .replace("%1", commandWithClass.getAlias())
                                                .replace("%2", e.getMessage()), e);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    /*
                     * Random number/XP (or lucky)
                     */
                    Random random = new Random();
                    int low = 0, high = 12, lowLucky = 0, highLucky = 100;
                    int randomInt = random.nextInt(high - low) + low;
                    int randomXP = random.nextInt(highLucky - lowLucky) + lowLucky;

                    if (randomInt > randomXP) {
                        sendReply(messages[randomInt], update);
                    }
                }
            }
        }.init(this)).start();
    }

    public int sendMessage(String msg, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.enableMarkdown(true);
        sendMessage.disableWebPagePreview();

        try {
            return executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
        return 0;
    }

    public void sendMessageSync(SendMessage message, Update update) {
        /*
         * Don't use Async because it repeats the same message for unknown reason
         */
        try {
            execute(message);
        } catch (TelegramApiException telegramApiException) {
            logger.error(telegramApiException.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }

    public void deleteMessage(String chatID, Integer messageID, Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(messageID);
        deleteMessage.setChatId(chatID);

        try {
            executeAsync(deleteMessage);
        } catch (TelegramApiException telegramApiException) {
            logger.error(telegramApiException.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }


    public int sendReply(String msg, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
        sendMessage.disableWebPagePreview();

        try {
            return executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
        return 0;
    }

    public void sendReply2ID(String msg, int id, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyToMessageId(id);
        sendMessage.disableWebPagePreview();

        try {
            executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }

    // START: Until the bot uses HTML instead of Markdown
    public int sendReplyForShell(String msg, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
        sendMessage.disableWebPagePreview();

        try {
            return executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
        return 0;
    }

    public void editMessageForShell(String msg, Update update, int id) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(msg);
        editMessageText.setChatId(String.valueOf(update.getMessage().getChatId()));
        editMessageText.setMessageId(id);
        editMessageText.enableHtml(true);

        try {
            executeAsync(editMessageText);
        } catch (TelegramApiException telegramApiException) {
            logger.error(telegramApiException.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }
    // END: Until the bot uses HTML instead of Markdown

    public void editMessage(String msg, Update update, int id) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(msg);
        editMessageText.setChatId(String.valueOf(update.getMessage().getChatId()));
        editMessageText.setMessageId(id);
        editMessageText.enableMarkdown(true);

        try {
            executeAsync(editMessageText);
        } catch (TelegramApiException telegramApiException) {
            logger.error(telegramApiException.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }

    public boolean isPM(String userID, String chatID) {
        return !chatID.equals(userID);
    }

    public boolean isAdmin(String userID, String chatID) {
        if (userID.equals(chatID)) {
            return false;
        } else {
            try {
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId(chatID);
                getChatMember.setUserId(Long.valueOf(userID));

                ChatMember chatMember = execute(getChatMember);

                switch (chatMember.getStatus()) {
                    case "administrator":
                    case "creator":
                        return true;
                    default:
                        return false;
                }
            } catch (Exception exception) {
                logger.error(exception.getMessage() + " (CID: " + chatID + " | UID: " + userID + ")");
                return false;
            }
        }
    }

    private void runMethod(Class aClass, Update update, TelegramBot tBot, PrefObj prefs) {
        try {
            Object instance = ((Class<?>) aClass).getDeclaredConstructor().newInstance();
            Method method = ((Class<?>) aClass).getDeclaredMethod("botReply", Update.class, TelegramBot.class, PrefObj.class);
            method.invoke(instance, update, tBot, prefs);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public ArrayList<CommandWithClass> getActiveCommandsAsCmdObject() {
        ArrayList<CommandWithClass> allCommandsArObj = new ArrayList<>();
        for (Class clazz : commandClasses) {
            try {
                Object instance = ((Class<?>) clazz).getDeclaredConstructor().newInstance();
                Method methodAli = ((Class<?>) clazz).getSuperclass().getDeclaredMethod("getAlias");
                Method methodInf = ((Class<?>) clazz).getSuperclass().getDeclaredMethod("getCommandInfo");
                String alias = (String) methodAli.invoke(instance);
                String desc = (String) methodInf.invoke(instance);
                CommandWithClass c = new CommandWithClass(clazz, alias, desc);
                allCommandsArObj.add(c);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return allCommandsArObj;
    }

    private PrefObj getPrefs(Update update) {
        long chatId = update.getMessage().getChatId();
        PrefObj prefObj = DbThings.selectIntoPrefsTable(chatId);
        if (prefObj == null) {
            DbThings.insertIntoPrefsTable(chatId);
        }
        return prefObj;
    }

    @Override
    public String getBotUsername() {
        return bot.getUsername();
    }

    @Override
    public String getBotToken() {
        return bot.getToken();
    }

    public String getVersionID() {
        return bot.getVersionID();
    }
}