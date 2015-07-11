package com.vae.wuyunxing.webdav.mobile.main.message;

public class StartUploadEvent {
    public String selectLocalPath;

    public StartUploadEvent(String localPath) {
        this.selectLocalPath = localPath;
    }
}
