package com.callr.auth;

import java.util.*;
import com.callr.exceptions.CallrClientException;

public class LoginAs {

    private String type;
    private String target;

    private static HashMap<String, String> loginAsTypes = new HashMap<String,String>() {
        {
            put("user", "user.login");
            put("account", "account.hash");
        }
    };

    public LoginAs(String type, String target) throws CallrClientException {
        setLoginAs(type, target);
    }

    @Override
    public String toString(){
        return String.format("%s %s", loginAsTypes.get(this.type), this.target);
    }

    public void setLoginAs(String type, String target) throws CallrClientException {
        if(!loginAsTypes.containsKey(type.toLowerCase())){
            throw new CallrClientException("ERROR_IN_LOGINAS", -1, "type must be one of " + loginAsTypes.keySet().toString());
        } else {
            this.type = type.toLowerCase();
            this.target = target;
        }
    }
}