package com.thecallr;

public class ThecallrClientException extends ThecallrException {
	private static final long serialVersionUID = 6671688402507740577L;

	public ThecallrClientException(String message, int code, Object data) {
		super(message, code, data);
	}
}
