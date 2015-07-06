package com.vae.wuyunxing.webdav.library.exception;

public class IllegalDirectoryPathException extends Exception {

	private static final long serialVersionUID = -3745095366251061837L;

	public IllegalDirectoryPathException(String detailMessage) {
		super(detailMessage);
	}

	public IllegalDirectoryPathException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public IllegalDirectoryPathException(Throwable throwable) {
		super(throwable);
	}
}
