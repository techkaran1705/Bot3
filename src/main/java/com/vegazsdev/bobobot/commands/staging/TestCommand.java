package com.vegazsdev.bobobot.commands.staging;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.core.command.annotations.DisableCommand;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * That class is just for tests, ignore it, feel free to delete it in your source.
 */
@DisableCommand
@SuppressWarnings("unused")
public class TestCommand extends Command {

    public TestCommand() {
        super("test");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {}
}