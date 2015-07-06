package com.vae.wuyunxing.webdav.library;

import com.vae.wuyunxing.webdav.library.filter.AllFilter;
import com.vae.wuyunxing.webdav.library.filter.ApplicationFilter;
import com.vae.wuyunxing.webdav.library.filter.AudioFilter;
import com.vae.wuyunxing.webdav.library.filter.BitTorrentFilter;
import com.vae.wuyunxing.webdav.library.filter.DocumentFilter;
import com.vae.wuyunxing.webdav.library.filter.FileFilter;
import com.vae.wuyunxing.webdav.library.filter.ImageFilter;
import com.vae.wuyunxing.webdav.library.filter.VideoFilter;

public enum FileCategory {

	DOCUMENT,
	AUDIO,
	VIDEO,
	IMAGE,
	BIT_TORRENT,
	APPLICATION,
	OTHERS;

	private static final String[] VIDEO_SUFFIX = {"wmv",
												  "mp4",
												  "rmvb",
												  "rm",
												  "mpg",
												  "mpeg",
												  "asf",
												  "3gp",
												  "mov",
												  "avi",
												  "mkv",
												  "flv"};

	private static final String[] AUDIO_SUFFIX = {"wav",
												  "mp3",
												  "wma",
												  "aif"};

	private static final String[] IMAGE_SUFFIX = {"bmp",
												  "jpg",
												  "png",
												  "tiff",
												  "gif",
												  "psd",
												  "pcd",
												  "raw"};

	private static final String[] DOCUMENT_SUFFIX = {"txt",
													 "doc",
													 "ppt",
													 "xls",
													 "docx",
													 "pot",
													 "vsd",
													 "rtf",
													 "pdf",
													 "lrc",
													 "htm",
													 "html",
													 "chm"};

	private static final String[] BT_SUFFIX = {"torrent"};

	private static final String[] APP_SUFFIX = {"apk"};

	private static final String[] OTHERS_SUFFIX = {""};

	static {
		VIDEO.mSuffixes = VIDEO_SUFFIX;
		AUDIO.mSuffixes = AUDIO_SUFFIX;
		IMAGE.mSuffixes = IMAGE_SUFFIX;
		DOCUMENT.mSuffixes = DOCUMENT_SUFFIX;
		BIT_TORRENT.mSuffixes = BT_SUFFIX;
		APPLICATION.mSuffixes = APP_SUFFIX;
		OTHERS.mSuffixes = OTHERS_SUFFIX;

		VIDEO.mFilter = new VideoFilter();
		AUDIO.mFilter = new AudioFilter();
		IMAGE.mFilter = new ImageFilter();
		DOCUMENT.mFilter = new DocumentFilter();
		BIT_TORRENT.mFilter = new BitTorrentFilter();
		APPLICATION.mFilter = new ApplicationFilter();
		OTHERS.mFilter = new AllFilter();
	}

	private String[]   mSuffixes;
	private FileFilter mFilter;

	public String[] getSuffixes() {
		return mSuffixes;
	}

	public FileFilter getFilter() {
		return mFilter;
	}
}
