package com.vegazsdev.bobobot.core.gsi;

import org.telegram.telegrambots.meta.api.objects.Update;

public class GSICmdObj {

    private String url;
    private String gsi;
    private String param;
    private Update update;

    public GSICmdObj() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGsi() {
        return gsi;
    }

    public void setGsi(String gsi) {
        this.gsi = gsi;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }
}
