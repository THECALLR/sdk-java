package com.thecallr.exceptions;

public class ThecallrException extends Exception {
	public int					code;
	public Object				data;
	private static final long	serialVersionUID = 3199364728501511969L;

	public ThecallrException(String message, int code, Object data) {
		super(message);
		this.code = code;
		this.data = data;
	}
}
