package com.vegazsdev.bobobot.commands.gsi;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import com.vegazsdev.bobobot.utils.FileTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class SourceForgeSetup extends Command {

    private static final Logger logger = LoggerFactory.getLogger(SourceForgeSetup.class);

    public SourceForgeSetup() {
        super("sfs", "Setup sourceforge props");
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static String getSfConf(String prop) {
        FileInputStream fileInputStream = null;
        try {
            Properties getProps = new Properties();
            fileInputStream = new FileInputStream("configs/sf-creds.prop");
            getProps.load(fileInputStream);
            return getProps.getProperty(prop);
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }
        }
        return null;
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            if (FileTools.checkFileExistsCurPath("configs/sf-creds.prop")) {
                bot.sendMessage(prefs.getString("unable_to_create"), update);
            } else {
                mkSfConf();
                bot.sendMessage(prefs.getString("created_sf_folder"), update);
            }
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void mkSfConf() {
        FileOutputStream fileOutputStream = null;

        try {
            Properties saveProps = new Properties();

            if (!FileTools.checkFileExistsCurPath("configs/sf-creds.prop")) {
                saveProps.setProperty("bot-sf-user", "put your sf username");
                saveProps.setProperty("bot-sf-host", "frs.sourceforge.net");
                saveProps.setProperty("bot-sf-pass", "put your sf pass");
                saveProps.setProperty("bot-sf-proj", "put your sf project name");
                saveProps.setProperty("bot-send-announcement", "false");
                saveProps.setProperty("bot-announcement-id", "none");

                fileOutputStream = new FileOutputStream("configs/sf-creds.prop");
                saveProps.store(fileOutputStream, null);
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException ioException) {
                    logger.error(ioException.getMessage());
                }
            }
        }
    }
}