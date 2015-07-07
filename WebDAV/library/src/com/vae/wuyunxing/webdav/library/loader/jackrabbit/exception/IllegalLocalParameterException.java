package com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception;

public class IllegalLocalParameterException extends Exception {

	private static final long serialVersionUID = 1526350393832550662L;

	public IllegalLocalParameterException(String detailMessage) {
		super(detailMessage);
	}

	public IllegalLocalParameterException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public IllegalLocalParameterException(Throwable throwable) {
		super(throwable);
	}
}
