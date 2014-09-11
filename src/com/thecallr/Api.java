package com.thecallr;

import com.thecallr.exceptions.*;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Hashtable;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class Api {
	private final String					API_URL 	= "https://api.thecallr.com/";
	private String							_login 		= null;
	private String							_password 	= null;
	private Hashtable<String, String>		_config 	= null;

	/**
	 * Constructor
	 * @param String login
	 * @param String password
	 * @param Hashtable<String, String> config
	 */
	public									Api(String login, String password, Hashtable<String, String> config) {
		_login = login;
		_password = password;
		_config = config;
	}
	
	// overload Api constructor for optional parameters
	/**
	 * Constructor
	 * @param String login
	 * @param String password
	 */
	public									Api(String login, String password) {
		_login = login;
		_password = password;
	}

	// Send a request to THECALLR webservice
	/**
	 * call
	 * @param String methods
	 * @param ... params
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonElement						call(String method, Object... params) throws ThecallrException, ThecallrClientException {
		ArrayList array = new ArrayList();
		
		for (Object el : params) {
			array.add(el);
		}
		return send(method, array, 0);
	}

	// Send a request to THECALLR webservice
	/**
	 * send
	 * @param String method
	 * @param ArrayList params
	 * @param int id
	 */
	@SuppressWarnings("rawtypes")
	public JsonElement						send(String method, ArrayList params, int id) throws ThecallrException, ThecallrClientException {
		URL									url 			= null;
		HttpsURLConnection					conn 			= null;
		StringBuffer						response 		= null;
		String								tmp 			= null;
		Proxy								proxy 			= null;
		URI									proxy_uri 		= null;
		byte[]								postDataBytes 	= null;
		InputStream							in 				= null;
		DataOutputStream					out 			= null;
		BufferedReader						reader 			= null;
		Gson								gson 			= new Gson();

		try {
			url = new URL(API_URL);
		} catch (MalformedURLException e) {
			throw new ThecallrClientException("INVALID_API_URL", -1, null);
        }

		// Proxy support
		if (this._config != null && this._config.get("proxy") != null) {
			try {
				proxy_uri = new URI(this._config.get("proxy"));
				tmp = proxy_uri.getUserInfo();

				// proxy auth
				if (tmp != null) {
					final String[] data = tmp.split(":");
					Authenticator authenticator = new Authenticator() {
				        public PasswordAuthentication getPasswordAuthentication() {
				            return (new PasswordAuthentication(data[0], data[1].toCharArray()));
				        }
				    };
				    Authenticator.setDefault(authenticator);
				    tmp = null;
				}

				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxy_uri.getHost(), proxy_uri.getPort()));
			} catch (URISyntaxException e1) {
				throw new ThecallrClientException("INVALID_PROXY_URL", -1, null);
			}
		} else {
			proxy = Proxy.NO_PROXY;
		}

		// encode credentials to base64 Basic Auth format
		tmp = javax.xml.bind.DatatypeConverter.printBase64Binary((this._login + ":" + this._password).getBytes());

		try {
			postDataBytes = gson.toJson(createObject(method, params, id)).getBytes("UTF-8");
			conn = (HttpsURLConnection) url.openConnection(proxy);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json-rpc; charset=utf-8");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Authorization", "Basic " + tmp);
			conn.setRequestProperty("Content-Length", Integer.toString(postDataBytes.length));

			// Send request
			out = new DataOutputStream(conn.getOutputStream());
			out.write(postDataBytes);
			out.flush();
			out.close();

			// get response
			in = conn.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

			// get all lines
			tmp = null;
			response = new StringBuffer();
			while((tmp = reader.readLine()) != null) {
				response.append(tmp);
				response.append('\r');
			}
			reader.close();
			in.close();
			return parseResponse(response.toString());
		} catch (IOException e) {
			throw new ThecallrClientException("ERROR_IN_REQUEST", -1, e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}
	
	// overload send method for optional parameters
	@SuppressWarnings("rawtypes")
	/**
	 * @param String method
	 * @param Arraylist params
	 */
	public JsonElement						send(String method, ArrayList params) throws ThecallrException, ThecallrClientException {
		return send(method, params, 0);
	}

	@SuppressWarnings("rawtypes")
	private Hashtable<String, Object>		createObject(String method, ArrayList params, int id) {
		Hashtable<String, Object>			obj = new Hashtable<String, Object>();

		obj.put("jsonrpc", "2.0");
		obj.put("id", id != 0 ? id : 100 + (int)(Math.random() * ((999 - 100) + 1)));
		obj.put("method", method);
		obj.put("params", params);
		return obj;
	}

	// Response analysis
	private JsonElement						parseResponse(String response) throws ThecallrException {
		JsonElement			result = null;
		
		try {
			result = new JsonParser().parse(response);
		} catch (Exception e) {
			throw new ThecallrException("INVALID_RESPONSE", -1, response);
		}
		
		if (result != null && result.isJsonObject() && result.getAsJsonObject().has("result")) {
			return result.getAsJsonObject().get("result");
		} else if (result.isJsonObject() && result.getAsJsonObject().has("error")) {
			throw new ThecallrException(
				result.getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString(),
				result.getAsJsonObject().get("error").getAsJsonObject().get("code").getAsInt(),
				null
			);
		} else {
			throw new ThecallrException("INVALID_RESPONSE", -1, response);
		}
	}
}
