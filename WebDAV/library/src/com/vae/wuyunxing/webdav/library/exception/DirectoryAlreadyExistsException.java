package com.vae.wuyunxing.webdav.library.exception;

public class DirectoryAlreadyExistsException extends Exception {

	private static final long serialVersionUID = 3260799263072826803L;

	public DirectoryAlreadyExistsException(String detailMessage) {
		super(detailMessage);
	}

	public DirectoryAlreadyExistsException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DirectoryAlreadyExistsException(Throwable throwable) {
		super(throwable);
	}
}
