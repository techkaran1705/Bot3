package com.vegazsdev.bobobot.core.command;

@SuppressWarnings("rawtypes")
/*
 * Don't need to warn about unused methods, it's useless for now,
 * and it is unnecessary to warn about classes that are 'parameterized'
 */
public class CommandWithClass {

    private final Class clazz;
    private final String alias;
    private final String commandInfo;

    public CommandWithClass(Class clazz, String alias, String commandInfo) {
        this.clazz = clazz;
        this.alias = alias;
        this.commandInfo = commandInfo;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getAlias() {
        return alias;
    }

    public String getCommandInfo() {
        return commandInfo;
    }
}