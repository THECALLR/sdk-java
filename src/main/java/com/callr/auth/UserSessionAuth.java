package com.callr.auth;

public class UserSessionAuth extends CallrAuthentication {
    private String userSessionToken;

    public UserSessionAuth(String userSessionToken){
        this.userSessionToken = userSessionToken;
    }

    @Override
    public String toString(){
        return String.format("Session %s", this.userSessionToken);
    }
}
