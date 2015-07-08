package com.vae.wuyunxing.webdav.library.imp.jackrabbit;

import com.owncloud.android.lib.resources.files.RemoteFile;
import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.util.FileUtil;

import java.util.Calendar;


public class JackrabbitFileInfo implements FileInfo {

	private final RemoteFile   mRemoteFile;
	private final FileCategory mCategory;


	private JackrabbitFileInfo(RemoteFile file) {
		this.mRemoteFile = file;
		this.mCategory = FileUtil.filterFileCategory(file.getName());
	}

	public static JackrabbitFileInfo create(RemoteFile file) {
		return new JackrabbitFileInfo(file);
	}

	@Override
	public String getName() {
		return mRemoteFile.getName();
	}

	@Override
	public String getPath() {
		return mRemoteFile.getRemotePath();
	}

	@Override
	public String getParent() {
		return mRemoteFile.getParent();
	}

	@Override
	public boolean isDir() {
		return mRemoteFile.isDirectory();
	}

	@Override
	public boolean isFile() {
		return !mRemoteFile.isDirectory();
	}

	@Override
	public Calendar createTime() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mRemoteFile.getCreationTimestamp());
		return calendar;
	}

	@Override
	public Calendar lastModified() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mRemoteFile.getModifiedTimestamp());
		return calendar;
	}

	@Override
	public long size() {
		return mRemoteFile.getLength();
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getUri() {
		return mRemoteFile.getUri();
	}

	@Override
	public String suffix() {
		return FileUtil.getSuffix(getName());
	}

	@Override
	public FileCategory category() {
		return mCategory;
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
