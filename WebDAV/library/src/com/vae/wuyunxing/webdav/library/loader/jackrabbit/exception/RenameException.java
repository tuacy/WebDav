package com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception;

public class RenameException extends Exception {

	private static final long serialVersionUID = -8393044175570115021L;

	public RenameException(String detailMessage) {
		super(detailMessage);
	}

	public RenameException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RenameException(Throwable throwable) {
		super(throwable);
	}
}
