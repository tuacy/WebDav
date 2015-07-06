package com.vae.wuyunxing.webdav.library.imp.local;

import java.io.File;

public class LocalPath {
	private final String mParent;
	private final String mFilename;

	public LocalPath(String parent, String filename) {
		this.mParent = parent;
		this.mFilename = filename;
	}

	public String getParent() {
		return mParent;
	}

	public String getFilename() {
		return mFilename;
	}

	public String getFullPath() {
		return mParent + File.separator + mFilename;
	}
}
