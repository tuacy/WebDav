package com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception;

public class IllegalJackrabbitParameterException extends Exception {

	private static final long serialVersionUID = 888702812898078652L;

	public IllegalJackrabbitParameterException(String detailMessage) {
		super(detailMessage);
	}

	public IllegalJackrabbitParameterException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public IllegalJackrabbitParameterException(Throwable throwable) {
		super(throwable);
	}
}
