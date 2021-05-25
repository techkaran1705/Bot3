package com.vegazsdev.bobobot.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class Config {

    /**
     * Logger: To send warning, info & errors to terminal.
     */
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    public static String getDefConfig(String prop) {
        FileInputStream fileInputStream = null;
        try {
            Properties getProps = new Properties();
            fileInputStream = new FileInputStream("configs/configs.prop");
            getProps.load(fileInputStream);
            return getProps.getProperty(prop);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (fileInputStream != null) fileInputStream.close();
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            }
        }
        return null;
    }

    public void createDefConfig() {
        FileOutputStream fileOutputStream = null;
        try {
            FileTools.createFolder("configs");
            Properties saveProps = new Properties();
            saveProps.setProperty("bot-token", "put your telegram bot token here");
            saveProps.setProperty("bot-username", "put your bot user name");
            saveProps.setProperty("bot-master", "put your telegram user id here");
            saveProps.setProperty("requestChat", "put your main chat id for request here");
            saveProps.setProperty("publicChannel", "put id or username of channel which will be used to send GSI, ex: TrebleExperience");
            saveProps.setProperty("privateChat", "put your private (adm) chat id for request here");
            fileOutputStream = new FileOutputStream("configs/configs.prop");
            saveProps.store(fileOutputStream, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            }
        }
    }
}