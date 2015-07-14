package com.vae.wuyunxing.webdav.mobile.main.transfer.message;

public class TransferStartEvent {

	public final boolean mIsUpload;
	public final long    mID;
	public final boolean mIsSyncEvent;

	public TransferStartEvent(boolean isUpload, long id, boolean isSyncEvent) {
		mIsUpload = isUpload;
		mID = id;
		mIsSyncEvent = isSyncEvent;
	}
}
