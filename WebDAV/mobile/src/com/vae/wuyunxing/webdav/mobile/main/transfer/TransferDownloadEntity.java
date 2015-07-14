package com.vae.wuyunxing.webdav.mobile.main.transfer;

import java.util.Date;

import greendao.DownloadInfo;

public class TransferDownloadEntity implements TransferEntity {

	DownloadInfo downloadInfo;

	public TransferDownloadEntity(DownloadInfo downloadInfo) {
		this.downloadInfo = downloadInfo;
	}

	public DownloadInfo getDownloadInfo() {
		return downloadInfo;
	}

	@Override
	public Long getId() {
		return downloadInfo.getId();
	}

	@Override
	public void setId(Long id) {
		downloadInfo.setId(id);
	}

	@Override
	public String getUser() {
		return downloadInfo.getUser();
	}

	@Override
	public void setUser(String user) {
		downloadInfo.setUser(user);
	}

	@Override
	public String getFilename() {
		return downloadInfo.getFilename();
	}

	@Override
	public void setFilename(String filename) {
		downloadInfo.setFilename(filename);
	}

	@Override
	public String getFrom() {
		return downloadInfo.getFrom();
	}

	@Override
	public void setFrom(String from) {
		downloadInfo.setFrom(from);
	}

	@Override
	public String getTo() {
		return downloadInfo.getTo();
	}

	@Override
	public void setTo(String to) {
		downloadInfo.setFrom(to);
	}

	@Override
	public Date getUploadTime() {
		return downloadInfo.getUploadTime();
	}

	@Override
	public void setUploadTime(Date uploadTime) {
		downloadInfo.setUploadTime(uploadTime);
	}

	@Override
	public Integer getPercent() {
		return downloadInfo.getPercent();
	}

	@Override
	public void setPercent(Integer percent) {
		downloadInfo.setPercent(percent);
	}

	@Override
	public Integer getState() {
		return downloadInfo.getState();
	}

	@Override
	public void setState(Integer state) {
		downloadInfo.setState(state);
	}

	@Override
	public Boolean getAutoSync() {
		return downloadInfo.getAutoSyncDownload();
	}

	@Override
	public void setAutoSync(Boolean autoSyncUpload) {
		downloadInfo.setAutoSyncDownload(autoSyncUpload);
	}

	@Override
	public Long getTotalSize() {
		return downloadInfo.getTotalSize();
	}

	@Override
	public void setTotalSize(Long totalSize) {
		downloadInfo.setTotalSize(totalSize);
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
