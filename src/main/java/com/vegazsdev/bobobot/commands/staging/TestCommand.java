package com.vegazsdev.bobobot.commands.staging;

import com.vegazsdev.bobobot.TelegramBot;
import com.vegazsdev.bobobot.core.command.Command;
import com.vegazsdev.bobobot.core.command.annotations.DisableCommand;
import com.vegazsdev.bobobot.db.PrefObj;
import org.telegram.telegrambots.meta.api.objects.Update;

@DisableCommand
@SuppressWarnings("unused")
public class TestCommand extends Command {

    public TestCommand() {
        super("test", "Just a command for testing");
    }

    @Override
    public void botReply(Update update, TelegramBot bot, PrefObj prefs) {}
}