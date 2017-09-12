package com.callr.auth;

import org.apache.commons.codec.binary.Base64;


public class LoginPasswordAuth extends CallrAuthentication {
    private String login;
    private String password;

    public LoginPasswordAuth(String login, String password){
        this.login = login;
        this.password = password;
    }

    @Override
    public String toString(){
        String loginPassB64 = new String(Base64.encodeBase64((this.login + ":" + this.password).getBytes()));
        return String.format("Basic %s", loginPassB64);
    }
}