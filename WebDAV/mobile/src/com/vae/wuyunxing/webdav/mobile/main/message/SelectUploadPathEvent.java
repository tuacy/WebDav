package com.vae.wuyunxing.webdav.mobile.main.message;

public class SelectUploadPathEvent {

	public String uploadPath;//like: /1.txt
	public String displayPath;//like: smb://192.168.11.1/home/Hans/1.txt

	public SelectUploadPathEvent(String uploadPath, String displayPath) {
		this.uploadPath = uploadPath;
		this.displayPath = displayPath;
	}
}
