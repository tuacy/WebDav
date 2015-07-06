package com.vae.wuyunxing.webdav.mobile.main.message;

public class EditSelectionEvent {

	public final int mSelectionCount;
	public final int mTotalCount;
	
	public EditSelectionEvent(int selectionCount, int totalCount) {
		mSelectionCount = selectionCount;
		mTotalCount = totalCount;
	}
}
