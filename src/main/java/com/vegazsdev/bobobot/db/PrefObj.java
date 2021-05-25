package com.vegazsdev.bobobot.db;

import com.vegazsdev.bobobot.utils.XMLs;

public record PrefObj(double id, String lang, String hotkey, double ableToSendRandomMessage) {
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