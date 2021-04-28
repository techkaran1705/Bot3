package com.vegazsdev.bobobot;

import com.google.common.reflect.ClassPath;
import com.vegazsdev.bobobot.commands.owner.Chat2Shell;
import com.vegazsdev.bobobot.core.bot.Bot;
import com.vegazsdev.bobobot.core.bot.BuildInfo;
import com.vegazsdev.bobobot.db.DbThings;
import com.vegazsdev.bobobot.exception.BotTokenException;
import com.vegazsdev.bobobot.utils.Config;
import com.vegazsdev.bobobot.utils.FileTools;
import com.vegazsdev.bobobot.utils.XMLs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static String DEF_CORE_STRINGS_XML = "core-strings.xml";

    @SuppressWarnings({"SpellCheckingInspection", "UnstableApiUsage", "rawtypes"})
    public static void main(String[] args) {

        logger.info(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "bot_init"));

        if (!FileTools.checkFileExistsCurPath("configs/" + XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file"))) {
            logger.info(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file_not_found"));
            new Config().createDefConfig();
            logger.warn(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file_info"));
            if (!FileTools.checkFileExistsCurPath("configs/allowed2port.json")) Chat2Shell.runBash("echo \"[]\" >> configs/allowed2port.json");
            System.exit(0);
        }

        ArrayList<Class> commandClasses = new ArrayList<>();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith("com.vegazsdev.bobobot.commands")) {
                    final Class<?> clazz = info.load();
                    try {
                        commandClasses.add(clazz);
                        logger.info(Objects.requireNonNull(
                                XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "cc_init_cmd"))
                                .replace("%1", clazz.getSimpleName()));
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        if ((Config.getDefConfig("bot-token") != null && Objects.requireNonNull(Config.getDefConfig("bot-token")).contains(" "))
                || (Config.getDefConfig("bot-username") != null && Objects.requireNonNull(Config.getDefConfig("bot-username")).contains(" "))) {
            logger.warn(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "config_file_info"));
            System.exit(0);
        }

        BuildInfo buildInfo = new BuildInfo(false);

        Bot bot = null;
        try {
            bot = new Bot(
                    Objects.requireNonNull(Config.getDefConfig("bot-token")),
                    Config.getDefConfig("bot-username"),
                    buildInfo.getVersion());
        } catch (BotTokenException e) {
            logger.error(e.getMessage());
            System.exit(1);
        }

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

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot(bot, commandClasses));
            logger.info(XMLs.getFromStringsXML(DEF_CORE_STRINGS_XML, "bot_started"));
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
