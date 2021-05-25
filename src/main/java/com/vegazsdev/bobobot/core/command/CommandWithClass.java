package com.vegazsdev.bobobot.core.command;

@SuppressWarnings("rawtypes")
/*
 * Don't need to warn about unused methods, it's useless for now,
 * and it is unnecessary to warn about classes that are 'parameterized'
 */
public record CommandWithClass(Class clazz, String alias) {
    public Class getClazz() {
        return clazz;
    }
    public String getAlias() {
        return alias;
    }
}