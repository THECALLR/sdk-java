sdk-java
========

CALLR Java SDK

## Dependencies
The CALLR API uses JSON-RPC which needs to be parsed, google-gson is used for this task and
needs to be included in your classes, please see [https://github.com/google/gson](https://github.com/google/gson)
for more information.

The apache commons codec library is also required for Base64 encoding, please see
[https://commons.apache.org/proper/commons-codec/](https://commons.apache.org/proper/commons-codec/)
for more information.

The [jars](jars/) folder contains the jar of the CALLR SDK.

## Basic Example (Send SMS)
See full example in [samples](samples/)

```java
// Set your credentials
Api tc = new Api("login", "password");

Hashtable<String, Object> param = new Hashtable<String, Object>();
param.put("flash_message", false);

// 1. "call" method: each parameter of the method as an argument
result = tc.call("sms.send", "SMS", "+33123456789", "Hello, world", param);

// "sms.send" API method return a string so it need to be converted.
System.out.println(result.getAsString());

// 2. "send" method: parameter of the method is an array
ArrayList array = new ArrayList();
array.add("SMS");
array.add("+33123456789");
array.add("Hello, world");
array.add(param);

result = tc.send("sms.send", array);

System.out.println(result.getAsString());
```

## Authentication with Login as
### Using login as
```java
// Set your credentials
Api tc = new Api("login", "password");

// Set LoginAs
tc.setLoginAs("user", "<login>"); // available types: user, account
                                  // available targets: <login> for type user, <hash> for type account

result = tc.call("sms.send", "SMS", "+33123456789", "Hello, world", null);
System.out.println(result.getAsString());
...
```

### Reset login as
```java
// Set your credentials
Api tc = new Api("login", "password");

// Set LoginAs
tc.setLoginAs("user", "<login>"); // available types: user, account
                                  // available targets: <login> for type user, <hash> for type account

result = tc.call("sms.send", "SMS", "+33123456789", "Hello, world", null);
System.out.println(result.getAsString());
...
// Reset login as
tc.setLoginAs(null);
...
```

### Change login as
```java
// Set your credentials
Api tc = new Api("login", "password");

// Set LoginAs
tc.setLoginAs("user", "<login>"); // available types: user, account
                                  // available targets: <login> for type user, <hash> for type account

result = tc.call("sms.send", "SMS", "+33123456789", "Hello, world", null);
System.out.println(result.getAsString());
...
// Change login as
tc.setLoginAs("account","<account hash>");
...
```
