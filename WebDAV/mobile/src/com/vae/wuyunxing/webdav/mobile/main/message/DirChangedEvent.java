package com.vae.wuyunxing.webdav.mobile.main.message;

public class DirChangedEvent {

	public final boolean mIsRootDir;
	public final String mDir;

	public DirChangedEvent(boolean isRootDir, String dir) {
		mIsRootDir = isRootDir;
		mDir = dir;
	}
}
