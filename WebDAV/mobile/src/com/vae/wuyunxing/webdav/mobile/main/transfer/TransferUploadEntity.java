package com.vae.wuyunxing.webdav.mobile.main.transfer;

import com.vae.wuyunxing.webdav.mobile.main.transfer.TransferEntity;

import java.util.Date;

import greendao.UploadInfo;

public class TransferUploadEntity implements TransferEntity {

	private UploadInfo uploadInfo;

	public TransferUploadEntity(UploadInfo uploadInfo) {
		this.uploadInfo = uploadInfo;
	}

	public UploadInfo getUploadInfo() {
		return uploadInfo;
	}

	@Override
	public Long getId() {
		return uploadInfo.getId();
	}

	@Override
	public void setId(Long id) {
		uploadInfo.setId(id);
	}

	@Override
	public String getUser() {
		return uploadInfo.getUser();
	}

	@Override
	public void setUser(String user) {
		uploadInfo.setUser(user);
	}

	@Override
	public String getFilename() {
		return uploadInfo.getFilename();
	}

	@Override
	public void setFilename(String filename) {
		uploadInfo.setFilename(filename);
	}

	@Override
	public String getFrom() {
		return uploadInfo.getFrom();
	}

	@Override
	public void setFrom(String from) {
		uploadInfo.setFrom(from);
	}

	@Override
	public String getTo() {
		return uploadInfo.getTo();
	}

	@Override
	public void setTo(String to) {
		uploadInfo.setTo(to);
	}

	@Override
	public Date getUploadTime() {
		return uploadInfo.getUploadTime();
	}

	@Override
	public void setUploadTime(Date uploadTime) {
		uploadInfo.setUploadTime(uploadTime);
	}

	@Override
	public Integer getPercent() {
		return uploadInfo.getPercent();
	}

	@Override
	public void setPercent(Integer percent) {
		uploadInfo.setPercent(percent);
	}

	@Override
	public Integer getState() {
		return uploadInfo.getState();
	}

	@Override
	public void setState(Integer state) {
		uploadInfo.setState(state);
	}

	@Override
	public Boolean getAutoSync() {
		return uploadInfo.getAutoSyncUpload();
	}

	@Override
	public void setAutoSync(Boolean autoSyncUpload) {
		uploadInfo.setAutoSyncUpload(autoSyncUpload);
	}

	@Override
	public Long getTotalSize() {
		return uploadInfo.getTotalSize();
	}

	@Override
	public void setTotalSize(Long totalSize) {
		uploadInfo.setTotalSize(totalSize);
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + ((int) (this.getId() ^ (this.getId() >> 32)));
		result = 31 * result + (this.getFilename() == null ? 0 : this.getFilename().hashCode());
		result = 31 * result + (this.getFrom() == null ? 0 : this.getFrom().hashCode());
		result = 31 * result + (this.getTo() == null ? 0 : this.getTo().hashCode());
		return result;
	}
}
