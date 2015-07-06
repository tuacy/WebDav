package com.vae.wuyunxing.webdav.library.exception;

public class NoSuchFileException extends Exception {

	private static final long serialVersionUID = -6833442274986964508L;

	public NoSuchFileException(String detailMessage) {
		super(detailMessage);
	}

	public NoSuchFileException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public NoSuchFileException(Throwable throwable) {
		super(throwable);
	}
}
