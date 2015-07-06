package com.vae.wuyunxing.webdav.library;

import java.util.Calendar;

public class DotFile implements FileInfo {

	@Override
	public String getName() {
		return ".";
	}

	@Override
	public String getPath() {
		return ".";
	}

	@Override
	public String getParent() {
		return ".";
	}

	@Override
	public boolean isDir() {
		return true;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public Calendar createTime() {
		return Calendar.getInstance();
	}

	@Override
	public Calendar lastModified() {
		return Calendar.getInstance();
	}

	@Override
	public long size() {
		return 0;
	}

	@Override
	public boolean canRead() {
		return false;
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getUri() {
		return null;
	}

	@Override
	public String suffix() {
		return "";
	}

	@Override
	public FileCategory category() {
		return FileCategory.OTHERS;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof FileInfo)) {
			return false;
		}
		FileInfo file = (FileInfo) o;
		return (file.exists() == this.exists()) && (file.isDir() == this.isDir() || file.isFile() == this.isFile()) &&
			   (file.getPath() != null && this.getPath() != null) &&
			   file.getPath().equals(this.getPath()) && file.size() == this.size() && file.canRead() == this.canRead() &&
			   file.canWrite() == this.canWrite();
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 31 * result + (this.exists() ? 1 : 0);
		result = 31 * result + (this.isDir() ? 1 : 0);
		result = 31 * result + (this.getPath() == null ? 0 : this.getPath().hashCode());
		result = 31 * result + ((int) (this.size() ^ (this.size() >> 32)));
		result = 31 * result + (this.canRead() ? 1 : 0);
		result = 31 * result + (this.canWrite() ? 1 : 0);
		return result;
	}

}
