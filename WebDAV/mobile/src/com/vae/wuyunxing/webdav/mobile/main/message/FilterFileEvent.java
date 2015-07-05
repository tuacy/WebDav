package com.vae.wuyunxing.webdav.mobile.main.message;

public class FilterFileEvent {

	public static final int FILTER_TYPE_DOC   = 0;
	public static final int FILTER_TYPE_MUSIC = 1;
	public static final int FILTER_TYPE_VIDEO = 2;
	public static final int FILTER_TYPE_PHOTO = 3;
	public static final int FILTER_TYPE_BT    = 4;
	public static final int FILTER_TYPE_APP   = 5;
	public static final int FILTER_TYPE_ALL   = 6;

	public final int     mCategory;
	public final boolean mIsLocalFileFilterEvent;

	public FilterFileEvent(int category, boolean isLocalFileFilterEvent) {
		mCategory = category;
		mIsLocalFileFilterEvent = isLocalFileFilterEvent;
	}
}
