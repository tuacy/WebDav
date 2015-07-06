package com.vae.wuyunxing.webdav.library.filter;


import com.vae.wuyunxing.webdav.library.FileInfo;

public class AllFilter implements FileFilter {

	@Override
	public boolean accept(FileInfo file) {
		return true;
	}
}
