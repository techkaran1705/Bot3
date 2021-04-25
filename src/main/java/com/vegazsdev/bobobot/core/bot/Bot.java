package com.vegazsdev.bobobot.core.bot;

import com.vegazsdev.bobobot.exception.BotTokenException;

public class Bot {

    public String token;
    public String username;

    public Bot(String token, String username) throws BotTokenException {
        if (!(token.length() >= 46)) {
            throw new BotTokenException("The bot token usually has a length greater than or equal to 46 characters");
        } else {
            this.token = token;
            this.username = username;
        }
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username.replace("@", "");
    }
}