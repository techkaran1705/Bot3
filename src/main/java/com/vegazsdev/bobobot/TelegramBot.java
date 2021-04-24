package com.vegazsdev.bobobot;

import com.vegazsdev.bobobot.core.Bot;
import com.vegazsdev.bobobot.core.CommandWithClass;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.db.PrefObj;
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
import java.util.concurrent.ExecutionException;

@SuppressWarnings("rawtypes")
public class TelegramBot extends TelegramLongPollingBot {

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

                    String msg = update.getMessage().getText();
                    long usrId = update.getMessage().getFrom().getId();
                    PrefObj chatPrefs = getPrefs(update);

                    if (chatPrefs == null) {
                        chatPrefs = new PrefObj(0, "strings-en.xml", "!");
                    }

                    if (msg.startsWith(Objects.requireNonNull(chatPrefs.getHotkey()))) {
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
        } catch (TelegramApiException | ExecutionException | InterruptedException e) {
            logger.error(e.getMessage());
        }
        return 0;
    }

    public void sendMessageSync(SendMessage message) {
        /*
         * Don't use Async because it repeats the same message for unknown reason
         */
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
        }
    }

    public void deleteMessage(String chatID, Integer messageID) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(messageID);
        deleteMessage.setChatId(chatID);

        try {
            executeAsync(deleteMessage);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage(), e);
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
        } catch (TelegramApiException | ExecutionException | InterruptedException e) {
            logger.error(e.getMessage());
        }
        return 0;
    }

    public void sendMessage2ID(String msg, long id) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(msg);
        sendMessage.setChatId(String.valueOf(id));
        sendMessage.enableMarkdown(true);
        sendMessage.disableWebPagePreview();

        try {
            executeAsync(sendMessage).get().getMessageId();
        } catch (TelegramApiException | ExecutionException | InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    public void editMessage(String msg, Update update, int id) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setText(msg);
        editMessageText.setChatId(String.valueOf(update.getMessage().getChatId()));
        editMessageText.setMessageId(id);
        editMessageText.enableMarkdown(true);

        try {
            executeAsync(editMessageText);
        } catch (TelegramApiException e) {
            logger.error(e.getMessage());
        }
    }

    public boolean isUserAdminOrPV(Update update) {
        String userID = update.getMessage().getFrom().getId().toString();
        String chatID = update.getMessage().getChat().getId().toString();

        if (userID.equals(chatID)) {
            return true;
        } else {
            try {
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId(String.valueOf(update.getMessage().getChatId()));
                getChatMember.setUserId(update.getMessage().getFrom().getId());

                ChatMember chatMember = execute(getChatMember);

                switch (chatMember.getStatus()) {
                    case "administrator":
                    case "creator":
                        return true;
                    default:
                        return false;
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
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
}