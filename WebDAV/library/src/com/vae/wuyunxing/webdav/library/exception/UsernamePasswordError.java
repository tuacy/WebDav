package com.vae.wuyunxing.webdav.library.exception;

public class UsernamePasswordError extends Exception {

	private static final long serialVersionUID = 6215362669096899103L;

	public UsernamePasswordError(String detailMessage) {
		super(detailMessage);
	}

	public UsernamePasswordError(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public UsernamePasswordError(Throwable throwable) {
		super(throwable);
	}
}
