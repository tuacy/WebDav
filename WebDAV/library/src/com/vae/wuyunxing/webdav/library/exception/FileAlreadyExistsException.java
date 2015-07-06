package com.vae.wuyunxing.webdav.library.exception;

public class FileAlreadyExistsException extends Exception {

	private static final long serialVersionUID = 7132828977069622037L;

	public FileAlreadyExistsException(String detailMessage) {
		super(detailMessage);
	}

	public FileAlreadyExistsException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public FileAlreadyExistsException(Throwable throwable) {
		super(throwable);
	}
}
