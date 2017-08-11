package com.callr;

import com.callr.exceptions.*;
import com.callr.auth.*;

import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;

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
	private static final String				SDK_VERSION = "1.4.1";
	private String							_apiUrl 	= "https://api.callr.com/json-rpc/v1.1/";
	private String							_login 		= null;
	private String							_password 	= null;
	private Hashtable<String, String>		_config 	= null;
	private LoginAs							_logAs  	= null;

	/**
	 * Constructor
	 * @param login CALLR login
	 * @param password CALLR password
	 * @param config extra configuration options
	 */
	public									Api(String login, String password, Hashtable<String, String> config) {
		_login = login;
		_password = password;
		_config = config;
	}

	// overload Api constructor for optional parameters
	/**
	 * Constructor
	 * @param login
	 * @param password
	 */
	public									Api(String login, String password) {
		this(login, password, null);
	}

	/**
	 * setApiUrl
	 * @param url
	 */
	 public void 							setApiUrl(String url){
		 this._apiUrl = url;
	 }

	 /**
	  * setLoginAs - set to null to clear
	  * @param LoginAs
	  */
	 public void 							setLoginAs(LoginAs logAs){
		this._logAs = logAs;
	 }

	 /**
	  * setLoginAs overload for updating object
	  */
	public void 							setLoginAs(String type, String target) throws CallrClientException {
		this._logAs = new LoginAs(type, target);
	}

	// Send a request to CALLR webservice
	/**
	 * call
	 * @param method
	 * @param params
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public JsonElement call(String method, Object... params) throws CallrException, CallrClientException {
		ArrayList array = new ArrayList();

		for (Object el : params) {
			array.add(el);
		}
		return send(method, array, 0);
	}

	// Send a request to CALLR webservice
	/**
	 * send
	 * @param method
	 * @param params
	 * @param id
	 */
	@SuppressWarnings("rawtypes")
	public JsonElement						send(String method, ArrayList params, int id) throws CallrException, CallrClientException {
		URL									url 			= null;
		HttpsURLConnection					conn 			= null;
		StringBuffer						response 		= null;
		String								tmp 			= null;
		Proxy								proxy 			= null;
		URI									proxyUri 		= null;
		byte[]								postDataBytes 	= null;
		InputStream							in 				= null;
		DataOutputStream					out 			= null;
		BufferedReader						reader 			= null;
		Gson								gson 			= new Gson();

		try {
			url = new URL(_apiUrl);
		} catch (MalformedURLException e) {
			throw new CallrClientException("INVALID_API_URL", -1, null);
		}

		// Proxy support
		if (this._config != null && this._config.get("proxy") != null) {
			try {
				proxyUri = new URI(this._config.get("proxy"));
				tmp = proxyUri.getUserInfo();

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

				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort()));
			} catch (URISyntaxException e1) {
				throw new CallrClientException("INVALID_PROXY_URL", -1, null);
			}
		} else {
			proxy = Proxy.NO_PROXY;
		}

		// encode credentials to base64 Basic Auth format
		tmp = new String(Base64.encodeBase64((this._login + ":" + this._password).getBytes()));

		try {
			postDataBytes = gson.toJson(createObject(method, params, id)).getBytes("UTF-8");
			conn = (HttpsURLConnection) url.openConnection(proxy);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json-rpc; charset=utf-8");
			conn.setRequestProperty("User-Agent", "sdk=JAVA; sdk-version="+SDK_VERSION+"; lang-version="+System.getProperty("java.version")+"; platform="+System.getProperty("os.name"));
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Authorization", "Basic " + tmp);
			conn.setRequestProperty("Content-Length", Integer.toString(postDataBytes.length));

			// Check for LoginAs
			if(this._logAs != null){
				conn.setRequestProperty("CALLR-Login-As", _logAs.toString());
			}

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
			throw new CallrClientException("ERROR_IN_REQUEST", -1, e.getMessage());
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	// overload send method for optional parameters
	@SuppressWarnings("rawtypes")
	/**
	 * @param method
	 * @param params
	 */
	public JsonElement						send(String method, ArrayList params) throws CallrException, CallrClientException {
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
	private JsonElement						parseResponse(String response) throws CallrException {
		JsonElement			result = null;

		try {
			result = new JsonParser().parse(response);
		} catch (Exception e) {
			throw new CallrException("INVALID_RESPONSE", -1, response);
		}

		if (result != null && result.isJsonObject() && result.getAsJsonObject().has("result")) {
			return result.getAsJsonObject().get("result");
		} else if (result.isJsonObject() && result.getAsJsonObject().has("error")) {
			throw new CallrException(
				result.getAsJsonObject().get("error").getAsJsonObject().get("message").getAsString(),
				result.getAsJsonObject().get("error").getAsJsonObject().get("code").getAsInt(),
				null
			);
		} else {
			throw new CallrException("INVALID_RESPONSE", -1, response);
		}
	}
}
