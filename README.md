sdk-java
========

SDK in Java for THECALLR API

## Dependencies
The CALLR API is in JSON which need to be parsed.
This SDK is based on google-gson to parse json.
So it need to be added as well as our SDK in you project see [https://github.com/google/gson](https://github.com/google/gson) for more information.

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