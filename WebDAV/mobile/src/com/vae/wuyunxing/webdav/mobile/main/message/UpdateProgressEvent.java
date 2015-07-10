package com.vae.wuyunxing.webdav.mobile.main.message;

public class UpdateProgressEvent {

	public final boolean mIsUpload;
	public final long    mID;
	public final int     mProgress;
	public final String  mFilename;
	public final boolean mIsSyncEvent;

	public UpdateProgressEvent(boolean isUpload, long id, int progress, String filename, boolean isSyncEvent) {
		mIsUpload = isUpload;
		mID = id;
		mProgress = progress;
		mFilename = filename;
		mIsSyncEvent = isSyncEvent;
	}
}
