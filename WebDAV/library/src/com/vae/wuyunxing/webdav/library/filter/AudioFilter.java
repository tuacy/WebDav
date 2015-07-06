package com.vae.wuyunxing.webdav.library.filter;


import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileInfo;

public class AudioFilter implements FileFilter {

	@Override
	public boolean accept(FileInfo file) {
		return file.isDir() || file.category() == FileCategory.AUDIO;
	}
}
