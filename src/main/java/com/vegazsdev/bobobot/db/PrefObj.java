package com.vegazsdev.bobobot.db;

import com.vegazsdev.bobobot.utils.XMLs;

public class PrefObj {

    private final double id;
    private final String lang;
    private final String hotkey;
    private final double ableToSendRandomMessage;

    public PrefObj(double id, String lang, String hotkey, double ableToSendRandomMessage) {
        this.id = id;
        this.lang = lang;
        this.hotkey = hotkey;
        this.ableToSendRandomMessage = ableToSendRandomMessage;
    }

    public double getAbleToSendRandomMessage() {
        return ableToSendRandomMessage;
    }

    public double getId() {
        return id;
    }

    public String getLang() {
        return lang;
    }

    public String getString(String value) {
        if (XMLs.getFromStringsXML(this.getLang(), value) == null) {
            return XMLs.getFromStringsXML("strings-en.xml", value);
        } else {
            return XMLs.getFromStringsXML(this.getLang(), value);
        }
    }

    public String getHotkey() {
        return hotkey;
    }
}