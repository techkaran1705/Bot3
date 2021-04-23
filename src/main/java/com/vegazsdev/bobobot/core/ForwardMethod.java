package com.vegazsdev.bobobot.core;

@SuppressWarnings({"unused", "rawtypes"})
/*
 * Don't need to warn about unused methods, it's useless for now,
 * and it is unnecessary to warn about classes that are 'parameterized'
 */
public class ForwardMethod {

    private final Class XClass;
    private String alias;

    public ForwardMethod(Class aClass, String alias) {
        this.XClass = aClass;
    }

    public Class getXClass() {
        return XClass;
    }

    public String getAlias() {
        return alias;
    }
}