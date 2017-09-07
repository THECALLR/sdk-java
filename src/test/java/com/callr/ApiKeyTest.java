package com.callr;

import com.callr.Api;
import com.callr.auth.ApiKeyAuth;
import com.callr.exceptions.CallrException;
import com.google.gson.JsonElement;

import static org.junit.Assert.*;

import java.util.Date;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ApiKeyTest extends CallrBase {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static ApiKey apiKey;
  private static Api apiWithKey;
  private static final String keyName = new Date().toString() + "callr-test-key";

  @Test
  public void aTestApiKeyFormatAndLength() throws CallrException {
    JsonElement res = api.call("api-key.create", keyName);
    apiKey = gson.fromJson(res.toString(), ApiKey.class);

    assertTrue("Api key is only hex characters", apiKey.token.matches("^[0-9A-Fa-f]+$"));
    assertTrue("Api key is 100 characters", apiKey.token.length() == 100);
  }

  @Test
  public void bTestApiKeyValid() throws CallrException {
    apiWithKey = new Api(new ApiKeyAuth(apiKey.token), null);
    assertNotNull("Checking Api-Key is valid", apiWithKey.call("system.get_timestamp"));

    JsonElement res = apiWithKey.call("api-key.all");
    ApiKey[] accountKeys = gson.fromJson(res.toString(), ApiKey[].class);
    Boolean apiKeyFound = false;
    for (int i = 0; i < accountKeys.length; i++) {
      if (accountKeys[i].name.equals(keyName)) {
        apiKeyFound = true;
        break;
      }
    }
    assertTrue(String.format("Created Api key name matches created name: '%s'", apiKey.name), apiKeyFound);
  }

  @Test
  public void cTestApiKeyDelete() throws CallrException {
    JsonElement res = apiWithKey.call("api-key.delete", apiKey.id);
    assertTrue("Api key deleted", res.getAsBoolean());

    res = api.call("api-key.all");
    ApiKey[] accountKeys = gson.fromJson(res.toString(), ApiKey[].class);
    Boolean apiKeyFound = false;
    for (int i = 0; i < accountKeys.length; i++) {
      if (accountKeys[i].id == apiKey.id) {
        apiKeyFound = true;
        break;
      }
    }
    assertFalse(String.format("Created Api key id still exists: %d", apiKey.id), apiKeyFound);

    // should throw exception because api key has been deleted
    thrown.expect(com.callr.exceptions.CallrClientException.class);
    thrown.expectMessage("ERROR_IN_REQUEST");
    apiWithKey.call("system.get_timestamp");
  }
}

class ApiKey {
  int id;
  String name;
  String token;

  @Override
  public String toString() {
    return String.format("%d %s", id, name);
  }
}