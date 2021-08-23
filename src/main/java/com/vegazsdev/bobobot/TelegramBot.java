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
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@SuppressWarnings("rawtypes")
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final Bot bot;
    private PrefObj chatPrefs;
    private ArrayList<Class> commandClasses;

    TelegramBot(Bot bot, ArrayList<Class> commandClasses) {
        this.bot = bot;
        this.commandClasses = commandClasses;
    }

    public TelegramBot(Bot bot) {
        this.bot = bot;
    }

    public static PrefObj getPrefs(double chatId) {
        PrefObj prefObj = DbThings.selectIntoPrefsTable(chatId);
        if (prefObj == null) {
            DbThings.insertIntoPrefsTable(chatId);
        }
        return prefObj;
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
                            + "hotkey text DEFAULT '/',"
                            + "lang text DEFAULT 'strings-en.xml'"
                            + ");"
            );
        }

        if (update.getMessage() != null) {
            /*
             * PrefObj, chatPrefs
             */
            chatPrefs = getPrefs(Double.parseDouble(Objects.requireNonNull(update.getMessage().getChatId().toString())));

            /*
             * Check if exists that chat in our db
             */
            if (chatPrefs == null) {
                try {
                    chatPrefs = new PrefObj(0, "strings-en.xml", "/");
                } catch (Exception exception) {
                    logger.error(exception.getMessage());
                }
            }

            /*
             * Create thread to run commands (it can run without thread)
             */
            new Thread(new Runnable() {
                private TelegramBot tBot;

                /*
                 * Create TelegramBot object using init()
                 */
                Runnable init(TelegramBot tBot) {
                    this.tBot = tBot;
                    return this;
                }

                /*
                 * Check conditional, command & other things
                 */
                @Override
                public void run() {
                    if (update.hasMessage() && update.getMessage().getText() != null
                            && !update.getMessage().getText().equals("")
                            && Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "possible_hotkeys"))
                            .indexOf(update.getMessage().getText().charAt(0)) >= 0) {

                        String msg = update.getMessage().getText();
                        long usrId = update.getMessage().getFrom().getId();

                        /*
                         * It is ok to run and send command
                         */
                        if (chatPrefs.getHotkey() != null && msg.startsWith(Objects.requireNonNull(chatPrefs.getHotkey()))) {
                            for (CommandWithClass commandWithClass : getActiveCommandsAsCmdObject()) {
                                String adjustCommand = msg.replace(Objects.requireNonNull(chatPrefs.getHotkey()), "");

                                if (adjustCommand.contains(" ")) {
                                    adjustCommand = adjustCommand.split(" ")[0];
                                }

                                if (commandWithClass.getAlias().equals(adjustCommand)) {
                                    try {
                                        logger.info(Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "command_ok"))
                                                .replace("%1", update.getMessage().getFrom().getFirstName() + " (" + usrId + ")")
                                                .replace("%2", adjustCommand));
                                        runMethod(commandWithClass.getClazz(), update, tBot, chatPrefs);
                                    } catch (Exception e) {
                                        logger.error(Objects.requireNonNull(XMLs.getFromStringsXML(Main.DEF_CORE_STRINGS_XML, "command_failure"))
                                                .replace("%1", commandWithClass.getAlias())
                                                .replace("%2", e.getMessage()), e);
                                    }
                                }
                            }
                        }
                    }
                }
            }.init(this)).start();
        }
    }

    public void sendMessageAsync(String msg, Update update) {
        /*
         * Prepare SendMessage base
         */
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.enableMarkdown(true);
        sendMessage.disableWebPagePreview();

        /*
         * Execute executeAsync() method
         */
        try {
            executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }

    public int sendMessageAsyncBase(SendMessage sendMessage, Update update) {
        /*
         * Execute executeAsync() method & use existent SendMessage object
         */
        try {
            return executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
        return 0;
    }

    public void deleteMessage(String chatID, Integer messageID, Update update) {
        /*
         * Prepare DeleteMessage base
         */
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(messageID);
        deleteMessage.setChatId(chatID);

        /*
         * Execute executeAsync() method
         */
        try {
            executeAsync(deleteMessage);
        } catch (TelegramApiException telegramApiException) {
            logger.error(telegramApiException.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }

    public int sendReply(String msg, Update update) {
        /*
         * Prepare SendMessage base
         */
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyToMessageId(update.getMessage().getMessageId());
        sendMessage.disableWebPagePreview();

        /*
         * Execute executeAsync() method
         */
        try {
            return executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
        return 0;
    }

    public void sendReply2ID(String msg, int id, Update update) {
        /*
         * Prepare SendMessage base
         */
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.enableHtml(true);
        sendMessage.setReplyToMessageId(id);
        sendMessage.disableWebPagePreview();

        /*
         * Execute executeAsync() method
         */
        try {
            executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException exception) {
            logger.error(exception.getMessage() + " (CID: " + update.getMessage().getChat().getId() + " | UID: " + update.getMessage().getFrom().getId() + ")");
        }
    }

    public void editMessage(String msg, Update update, int id) {
        /*
         * Prepare EditMessageText base
         */
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(msg);
        editMessageText.setChatId(String.valueOf(update.getMessage().getChatId()));
        editMessageText.setMessageId(id);
        editMessageText.enableHtml(true);

        /*
         * Execute executeAsync() method
         */
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
            /*
             * https://github.com/TrebleExperience/Bot3/commit/0f31e973edecce5ea25a92a6b3b841aaae1b333c
             */
            return false;
        } else {
            try {
                /*
                 * Create GetChatMember() object to get info of the user
                 */
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId(chatID);
                getChatMember.setUserId(Long.valueOf(userID));

                /*
                 * Execute GetChatMember() (to get info) using ChatMember().execute()
                 */
                ChatMember chatMember = execute(getChatMember);

                /*
                 * If executed fine, we'll be able to check status
                 */
                return switch (chatMember.getStatus()) {
                    case "administrator", "creator" -> true;
                    default -> false;
                };
            } catch (Exception exception) {
                logger.error(exception.getMessage() + " (CID: " + chatID + " | UID: " + userID + ")");
                return false;
            }
        }
    }

    public boolean leaveChat(String chatID) {
        /*
         * Prepare LeaveChat base
         */
        LeaveChat leaveChat = new LeaveChat();
        leaveChat.setChatId(chatID);

        /*
         * Execute executeAsync() method
         */
        try {
            return executeAsync(leaveChat).thenApply(response -> /* Just a workaround */ response.toString().equals("true")).get();
        } catch (TelegramApiException | ExecutionException | InterruptedException telegramApiException) {
            logger.error(telegramApiException.getMessage() + " (CID to leave: " + chatID + ")");
            return false;
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
                String alias = (String) methodAli.invoke(instance);
                CommandWithClass c = new CommandWithClass(clazz, alias);
                allCommandsArObj.add(c);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return allCommandsArObj;
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
