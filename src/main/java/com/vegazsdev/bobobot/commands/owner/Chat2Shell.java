package com.vegazsdev.bobobot.commands.owner;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

@SuppressWarnings("unused")
public class Chat2Shell extends Command {

    private static final Logger logger = LoggerFactory.getLogger(Download2GDrive.class);

    public Chat2Shell() {
        super("shell", "Run shell (bash) commands via chat");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            String msg = update.getMessage().getText().substring(7);

            ProcessBuilder processBuilder;
            processBuilder = new ProcessBuilder("/bin/bash", "-c", msg);

            StringBuilder fullLogs = new StringBuilder();
            fullLogs.append("***$ ").append(msg).append("***\n");

            int id = bot.sendReply(fullLogs.toString(), update);

            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;
            try {
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                inputStream = process.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                bufferedReader = new BufferedReader(inputStreamReader);

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    fullLogs.append("`").append(line).append("`").append("\n");
                    bot.editMessage(fullLogs.toString(), update, id);
                }
            } catch (Exception e) {
                bot.sendMessage(prefs.getString("something_went_wrong"), update);
                logger.error(e.getMessage(), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ioException) {
                        logger.error(ioException.getMessage(), ioException);
                    }
                }

                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException ioException) {
                        logger.error(ioException.getMessage(), ioException);
                    }
                }

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException ioException) {
                        logger.error(ioException.getMessage(), ioException);
                    }
                }
            }
        }
    }

    public static String runBash(String command) {
        StringBuilder baseCommand = new StringBuilder();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            /*
             * Process base
             */
            ProcessBuilder pb;
            pb = new ProcessBuilder("/bin/bash", "-c", command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            /*
             * Stream base
             */
            inputStream = process.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                baseCommand.append(line);
            }
            return String.valueOf(baseCommand);
        } catch (Exception exception) {
            logger.error(exception.getMessage(), exception);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }

            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage(), ioException);
                }
            }
        }
        return null;
    }
}