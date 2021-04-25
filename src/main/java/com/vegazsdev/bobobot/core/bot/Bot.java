package com.vegazsdev.bobobot.core.bot;

public class Bot {

    public String token;
    public String username;

    public Bot(String token, String username) {
        this.token = token;
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username.replace("@", "");
    }
}