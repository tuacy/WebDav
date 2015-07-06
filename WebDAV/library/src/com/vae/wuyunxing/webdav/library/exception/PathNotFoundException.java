package com.vae.wuyunxing.webdav.library.exception;

public class PathNotFoundException extends Exception {

	private static final long serialVersionUID = -1480014794813739351L;

	public PathNotFoundException(String detailMessage) {
		super(detailMessage);
	}

	public PathNotFoundException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public PathNotFoundException(Throwable throwable) {
		super(throwable);
	}
}
