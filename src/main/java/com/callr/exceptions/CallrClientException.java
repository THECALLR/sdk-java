package com.callr.exceptions;

public class CallrClientException extends CallrException {
	private static final long serialVersionUID = 6671688402507740577L;

	public CallrClientException(String message, int code, Object data) {
		super(message, code, data);
	}
}
