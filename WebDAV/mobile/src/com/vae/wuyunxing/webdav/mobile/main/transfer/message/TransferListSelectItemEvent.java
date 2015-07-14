package com.vae.wuyunxing.webdav.mobile.main.transfer.message;

public class TransferListSelectItemEvent {

	public int selectionCount;
	public int totalCount;

	public TransferListSelectItemEvent(int selectionCount, int totalCount) {
		this.selectionCount = selectionCount;
		this.totalCount = totalCount;
	}
}
