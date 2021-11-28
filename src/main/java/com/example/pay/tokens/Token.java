package com.example.pay.tokens;

public class Token {
    private String token;

    public Token(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return token;
    }
}
