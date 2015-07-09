package com.vae.wuyunxing.webdav.library.exception;

public class ConstructorException extends Exception{

	private static final long serialVersionUID = -6163561402102624498L;

	public ConstructorException(String detailMessage) {
		super(detailMessage);
	}

	public ConstructorException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ConstructorException(Throwable throwable) {
		super(throwable);
	}
}
