package com.callr;

import com.callr.Api;
import com.callr.auth.UserSessionAuth;
import com.callr.exceptions.CallrException;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;

import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserSessionKeyTest extends CallrBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static SessionKey sessKey;
  private static Api apiWithSession;

  @Test
  public void aTestKeyFormatAndLength() throws CallrException {
    JsonElement res = api.call("session.create");
    sessKey = gson.fromJson(res.toString(), SessionKey.class);
    assertTrue("Session key is only hex characters", sessKey.token.matches("^[0-9A-Fa-f]+$"));
    assertTrue(String.format("Session key is 40 characters: %d", sessKey.token.length()), sessKey.token.length() == 40);
  }

  @Test
  public void bTestSessionKeyValid() throws CallrException {
    apiWithSession = new Api(new UserSessionAuth(sessKey.token), null);
    assertNotNull("Checking Session is valid", apiWithSession.call("system.get_timestamp"));

    JsonElement res = apiWithSession.call("session.all");
    SessionKey[] sessionKeys = gson.fromJson(res.toString(), SessionKey[].class);
    Boolean keyFound = false;
    for (int i = 0; i < sessionKeys.length; i++) {
      if (sessionKeys[i].token.equals(sessKey.token))
        keyFound = true;
    }
    assertTrue("Returned key is in list of keys", keyFound);
  }

  @Test
  public void cTestSessionRefresh() throws CallrException, InterruptedException {
    Date validUntilOrig = (Date) sessKey.validUntil.clone();
    System.out.println(" - Sleeping for 5 seconds... please wait...");
    Thread.sleep(5000);
    JsonElement res = apiWithSession.call("session.refresh", sessKey.refreshToken);
    sessKey = gson.fromJson(res.toString(), SessionKey.class);
    assertTrue("token has been refreshed new valid_until is greater than old",
        sessKey.validUntil.after(validUntilOrig));
  }

  @Test
  public void dTestSessionDelete() throws CallrException {
    apiWithSession.call("session.delete");

    // should throw exception because api key has been deleted
    thrown.expect(com.callr.exceptions.CallrClientException.class);
    thrown.expectMessage("ERROR_IN_REQUEST");
    apiWithSession.call("system.get_timestamp");

    JsonElement res = api.call("session.all");
    SessionKey[] sessionKeys = gson.fromJson(res.toString(), SessionKey[].class);
    Boolean keyFound = false;
    for (int i = 0; i < sessionKeys.length; i++) {
      if (sessionKeys[i].token.equals(sessKey.token))
        keyFound = true;
    }
    assertFalse("Check if Session key has been removed", keyFound);
  }
}

class SessionKey {
  String token;
  @SerializedName("valid_until")
  Date validUntil;
  @SerializedName("refresh_token")
  String refreshToken;

  @Override
  public String toString() {
    return String.format("%s %s %s", token, refreshToken, validUntil);
  }
}