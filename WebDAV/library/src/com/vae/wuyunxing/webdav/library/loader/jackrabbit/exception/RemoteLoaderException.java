package com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception;

public class RemoteLoaderException extends Exception {

	private static final long serialVersionUID = -6080685746842514897L;

	public RemoteLoaderException(String detailMessage) {
		super(detailMessage);
	}

	public RemoteLoaderException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RemoteLoaderException(Throwable throwable) {
		super(throwable);
	}
}
