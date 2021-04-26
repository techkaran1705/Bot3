package com.vegazsdev.bobobot.commands.owner;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import com.vegazsdev.bobobot.utils.GDrive;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Stream;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class Download2GDrive extends Command {

    private static final Logger logger = LoggerFactory.getLogger(Download2GDrive.class);

    public Download2GDrive() {
        super("d2gdrive", "Download and Send a File to your own Google Drive" +
                " *only direct links are supported by now" +
                " *download are made using aria2c");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            if (update.getMessage().getText().contains(" ")) {
                try {
                    String msg = update.getMessage().getText().split(" ")[1];
                    ProcessBuilder pb;

                    bot.sendMessage(prefs.getString("d2g_downloading"), update);
                    pb = new ProcessBuilder("/bin/bash", "-c", "aria2c " + msg + " -d 'downloads/'");

                    try {
                        FileUtils.deleteDirectory(new File("downloads"));
                        Process process = pb.start();
                        process.waitFor();
                    } catch (Exception exception) {
                        logger.error(exception.getMessage());
                    }

                    ArrayList<String> var = new ArrayList<>();

                    try (Stream<Path> paths = Files.walk(Paths.get("downloads/"))) {
                        paths
                                .filter(Files::isRegularFile)
                                .forEach(a -> var.add(a.toString()));
                    } catch (IOException ioException) {
                        logger.error(ioException.getMessage());
                    }

                    try {
                        for (String sendFile : var) {
                            String fileTrim = sendFile.split("downloads/")[1];
                            File uploadFile = new File(sendFile);
                            GDrive.createGoogleFile(null, "application/octet-stream", fileTrim, uploadFile);
                            bot.sendMessage(prefs.getString("d2g_file_sent"), update);
                        }
                    } catch (Exception exception) {
                        logger.error(exception.getMessage());
                    }
                } catch (Exception e) {
                    bot.sendMessage(prefs.getString("something_went_wrong"), update);
                    logger.error(e.getMessage(), e);
                }
            } else {
                bot.sendMessage(prefs.getString("bad_usage"), update);
            }
        } else {
            bot.sendMessage(prefs.getString("only_master_can_run"), update);
        }
    }
}