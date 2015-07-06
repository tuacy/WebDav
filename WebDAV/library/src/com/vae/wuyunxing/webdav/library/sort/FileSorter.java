package com.vae.wuyunxing.webdav.library.sort;


import com.vae.wuyunxing.webdav.library.FileInfo;

import java.util.Comparator;

public enum FileSorter {

	FILE_NAME_ASCENDING,
	FILE_NAME_DESCENDING,
	FILE_SIZE_ASCENDING,
	FILE_SIZE_DESCENDING,
	FILE_DATE_ASCENDING,
	FILE_DATE_DESCENDING,
	FILE_SUFFIX_ASCENDING,
	FILE_SUFFIX_DESCENDING;

	static {
		FILE_NAME_ASCENDING.mSorter = new FileNameSorter(true);
		FILE_NAME_DESCENDING.mSorter = new FileNameSorter(false);
		FILE_SIZE_ASCENDING.mSorter = new FileSizeSorter(true);
		FILE_SIZE_DESCENDING.mSorter = new FileSizeSorter(false);
		FILE_DATE_ASCENDING.mSorter = new FileDateSorter(true);
		FILE_DATE_DESCENDING.mSorter = new FileDateSorter(false);
		FILE_SUFFIX_ASCENDING.mSorter = new FileSuffixSorter(true);
		FILE_SUFFIX_DESCENDING.mSorter = new FileSuffixSorter(false);
	}

	private Comparator<FileInfo> mSorter;

	public Comparator<FileInfo> getSorter() {
		return mSorter;
	}
}
