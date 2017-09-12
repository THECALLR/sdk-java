package com.callr.auth;

public class ApiKeyAuth extends CallrAuthentication {
    private String apiKey;

    public ApiKeyAuth(String apiKey){
        this.apiKey = apiKey;
    }

    @Override
    public String toString(){
        return String.format("Api-Key %s", this.apiKey);
    }

}