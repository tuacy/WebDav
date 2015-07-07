package com.vae.wuyunxing.webdav.library.loader.jackrabbit.test;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.transmission.JackrabbitDownloader;

public class JackrabbitDownloaderText extends JackrabbitDownloader {

	private Context mContext;

	public JackrabbitDownloaderText(Context context) {
		mContext = context;
	}

	@Override
	public Context getContext() {
		return mContext;
	}

	@Override
	protected LocalPath getLocalPath() {
		return new LocalPath(Environment.getExternalStorageDirectory().toString(), "124.mp3");
	}

	@Override
	protected JackrabbitPath getJackrabbitPath() {
		return new JackrabbitPath("192.168.11.1", JackrabbitPath.MOUNT_DIR + "/123.mp3", "root", "admin");
	}

	@Override
	protected void onProgressUpdate(int progress) {
		Log.d("vae_tag", "JackrabbitDownloaderText progress = " + progress);
	}

	@Override
	protected void onComplete() {
		Log.d("vae_tag", "JackrabbitDownloaderText uploader commplete");
	}

	@Override
	protected void onFailed() {
		Log.d("vae_tag", "JackrabbitDownloaderText uploader onFailed");
	}
}
