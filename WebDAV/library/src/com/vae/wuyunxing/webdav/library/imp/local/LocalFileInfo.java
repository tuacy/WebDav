package com.vae.wuyunxing.webdav.library.imp.local;

import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.util.FileUtil;

import java.io.File;
import java.util.Calendar;

public class LocalFileInfo implements FileInfo {

	private final File         mLocalFile;
	/** file category obtain from file suffix */
	private final FileCategory mCategory;

	private LocalFileInfo(File file) {
		this.mLocalFile = file;
		this.mCategory = FileUtil.filterFileCategory(file.getName());
	}

	public File getLocalFile() {
		return mLocalFile;
	}

	public FileCategory getCategory() {
		return mCategory;
	}

	public static LocalFileInfo create(File file) {
		return new LocalFileInfo(file);
	}

	@Override
	public String getName() {
		return mLocalFile.getName();
	}

	@Override
	public String getPath() {
		return mLocalFile.getPath();
	}

	@Override
	public String getParent() {
		return mLocalFile.getParent();
	}

	@Override
	public boolean isDir() {
		return mLocalFile.isDirectory();
	}

	@Override
	public boolean isFile() {
		return mLocalFile.isFile();
	}

	@Override
	public Calendar createTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);
		return calendar;
	}

	@Override
	public Calendar lastModified() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mLocalFile.lastModified());
		return calendar;
	}

	@Override
	public long size() {
		return mLocalFile.length();
	}

	@Override
	public boolean canRead() {
		return mLocalFile.canRead();
	}

	@Override
	public boolean canWrite() {
		return mLocalFile.canWrite();
	}

	@Override
	public boolean exists() {
		return mLocalFile.exists();
	}

	@Override
	public String getUri() {
		return null;
	}

	@Override
	public String suffix() {
		return FileUtil.getSuffix(getName());
	}

	@Override
	public FileCategory category() {
		return mCategory;
	}

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
