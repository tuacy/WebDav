package com.vae.wuyunxing.webdav.mobile.main.message;

public class StartDownloadEvent {

	public String selectLocalPath;

	public StartDownloadEvent(String localPath) {
		this.selectLocalPath = localPath;
	}
}
