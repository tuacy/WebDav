package com.vae.wuyunxing.webdav.mobile.main.message;

public class SelectRelativePathEvent {

	public String relativePath;//like: /1.txt
	public String displayPath;//like: smb://192.168.11.1/home/Hans/1.txt

	public SelectRelativePathEvent(String relativePath, String displayPath) {
		this.relativePath = relativePath;
		this.displayPath = displayPath;
	}
}
