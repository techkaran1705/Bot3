package com.vegazsdev.bobobot.commands.owner;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.db.PrefObj;
import com.vegazsdev.bobobot.utils.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Objects;

import static com.vegazsdev.bobobot.Main.shellStatus;

@SuppressWarnings("unused")
public class Eval extends Command {

    private static final Logger logger = LoggerFactory.getLogger(Eval.class);

    public Eval() {
        super("eval", "Run java commands with one command");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        String[] msgComparableRaw = update.getMessage().getText().split(" ");

        if (update.getMessage().getFrom().getId() == Float.parseFloat(Objects.requireNonNull(Config.getDefConfig("bot-master")))) {
            try {
                engine.eval("var imports = new JavaImporter(" +
                        "java," +
                        "java.io," +
                        "java.lang," +
                        "java.util," +
                        "Packages.com.vegazsdev.bobobot.commands" +
                        "Packages.org.telegram.telegrambots," +
                        "Packages.org.telegram.telegrambots.meta.api.methods," +
                        "Packages.org.telegram.telegrambots.meta.api.objects" +
                        ");"
                );

                engine.put("args", msgComparableRaw);
                engine.put("bot", bot);
                engine.put("shell", shellStatus);
                engine.put("engine", engine);
                engine.put("update", update);
                engine.put("prefs", prefs);

                String command = update.getMessage().getText().substring(msgComparableRaw[0].length());

                Object out = engine.eval(
                        "(function() {" +
                                "with (imports) {\n" +
                                command +
                                "\n}" +
                                "})();"
                );

                bot.sendMessageAsync(out == null ? "Executed without error." : out.toString(), update);
            } catch (Exception exception) {
                bot.sendMessageAsync("`" + exception.getMessage() + "`", update);
                logger.error(exception.getMessage());
            }
        }
    }
}