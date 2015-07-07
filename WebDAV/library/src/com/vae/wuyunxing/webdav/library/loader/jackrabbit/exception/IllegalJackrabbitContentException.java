package com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception;

public class IllegalJackrabbitContentException extends Exception {

	private static final long serialVersionUID = 8021438032067005456L;

	public IllegalJackrabbitContentException(String detailMessage) {
		super(detailMessage);
	}

	public IllegalJackrabbitContentException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public IllegalJackrabbitContentException(Throwable throwable) {
		super(throwable);
	}
}
