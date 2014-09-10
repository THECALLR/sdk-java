package sample;

import java.util.ArrayList;
import java.util.Hashtable;

import com.google.gson.JsonElement;
import com.thecallr.*;

public class Quickstart {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void main(String[] args) {
		JsonElement		result 		= null;
		Hashtable		options 	= null;

		try {
//			options = new Hashtable();
//			options.put("proxy", "http://foo:bar@example.com:8080");

			Api tc = new Api("login", "password", options);

			Hashtable<String, Object> param = new Hashtable<String, Object>();
			param.put("flash_message", false);

//			1. "call" method: each parameter of the method as an argument
			result = tc.call("sms.send", "THECALLR", "+33123456789", "Hello, world", param);

//			"sms.send" API method return a string so it need to be converted.
			System.out.println(result.getAsString());

//			For more complex object you need to convert each object
//			https://google-gson.googlecode.com/svn/trunk/gson/docs/javadocs/com/google/gson/JsonElement.html
//			Example
//			{
//			 	"array": ["value", "other"]
//			}
//			To get index 0 of "array" you should do:
//			System.out.println(result.getAsJsonObject().get("array").getAsJsonArray().get(0).getAsString());

//			2. "send" method: parameter of the method is an array
			ArrayList array = new ArrayList();
			array.add("THECALLR");
			array.add("+33123456789");
			array.add("Hello, world");
			array.add(param);

			result = tc.send("sms.send", array);

			System.out.println(result.getAsString());
			
		} catch (ThecallrException e) {
			System.out.println(e.getMessage());
			System.out.println(e.data);
		}
	}

}
