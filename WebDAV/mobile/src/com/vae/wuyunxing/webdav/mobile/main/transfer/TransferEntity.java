package com.vae.wuyunxing.webdav.mobile.main.transfer;

public interface TransferEntity {

	public Long getId();

	public void setId(Long id);

	public String getUser();

	public void setUser(String user);

	public String getFilename();

	public void setFilename(String filename);

	public String getFrom();

	public void setFrom(String from);

	public String getTo();

	public void setTo(String to);

	public java.util.Date getUploadTime();

	public void setUploadTime(java.util.Date uploadTime);

	public Integer getPercent();

	public void setPercent(Integer percent);

	public Integer getState();

	public void setState(Integer state);

	public Boolean getAutoSync();

	public void setAutoSync(Boolean autoSyncUpload);

	public Long getTotalSize();

	public void setTotalSize(Long totalSize);

	public int hashCode();
}
