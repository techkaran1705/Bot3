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

import static com.vegazsdev.bobobot.Main.shellStatus;

@SuppressWarnings("unused")
public class Chat2Shell extends Command {

    private static final Logger logger = LoggerFactory.getLogger(Chat2Shell.class);

    public Chat2Shell() {
        super("shell", "Run shell (bash) commands via chat");
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

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            if (shellStatus.canRun() && !shellStatus.isRunning()) {
                /*
                 * Lock status, until the Shell process ends
                 */
                shellStatus.lockStatus();

                String msg = update.getMessage().getText().substring(7);

                ProcessBuilder processBuilder;
                processBuilder = new ProcessBuilder("/bin/bash", "-c", msg);

                StringBuilder fullLogs = new StringBuilder();
                fullLogs.append("<code>").append(runBash("whoami")).append("</code>").append(" (<code>").append(runBash("uname -n")).append("</code>)").append(" ~ ").append(update.getMessage().getText().substring(7)).append("\n");

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
                        fullLogs.append("<code>").append(line).append("</code>").append("\n");
                        bot.editMessage(fullLogs.toString(), update, id);
                    }

                    process.waitFor();
                    bot.sendReply2ID(prefs.getString("return_code_shell")
                            .replace("%1", String.valueOf(process.exitValue())), id, update
                    );
                    shellStatus.unlockStatus();
                } catch (Exception e) {
                    if (!shellStatus.canRun())
                        shellStatus.unlockStatus();

                    bot.sendMessageAsync(prefs.getString("something_went_wrong"), update);
                    logger.error(e.getMessage());
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
            } else {
                bot.sendReply(prefs.getString("cant_run_shell"), update);
            }
        }
    }
}