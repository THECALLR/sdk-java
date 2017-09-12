package com.callr;

import com.callr.Api;
import com.callr.exceptions.CallrException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import static org.junit.Assert.*;

public class CallrBase {
    public static Api api;
    public static Gson gson = new Gson();

    public CallrBase() {
        String callrLogin = System.getenv("CALLR_LOGIN");
        assertNotNull("Check if CALLR_LOGIN in environment", callrLogin);
        assertTrue("Check CALLR_LOGIN length > 0", callrLogin.length() > 0);

        String callrPass = System.getenv("CALLR_PASS");
        assertNotNull("Check if CALLR_PASS in environment", callrPass);
        assertTrue("Check CALLR_PASS length > 0", callrPass.length() > 0);
        api = new Api(callrLogin, callrPass);

        try {
            JsonElement res = api.call("system.get_timestamp");
            assertNotNull(String.format("Checking credentials are valid", res.toString()), res);
        } catch (CallrException ex) {
        }

    }
}