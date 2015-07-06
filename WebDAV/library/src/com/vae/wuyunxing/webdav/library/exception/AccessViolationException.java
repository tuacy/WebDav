package com.vae.wuyunxing.webdav.library.exception;

public class AccessViolationException extends Exception {

	private static final long serialVersionUID = -1729236440526490317L;

	public AccessViolationException(String detailMessage) {
		super(detailMessage);
	}

	public AccessViolationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public AccessViolationException(Throwable throwable) {
		super(throwable);
	}
}
