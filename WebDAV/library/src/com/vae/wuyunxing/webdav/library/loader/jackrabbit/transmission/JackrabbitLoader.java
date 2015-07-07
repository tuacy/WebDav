package com.vae.wuyunxing.webdav.library.loader.jackrabbit.transmission;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.vae.wuyunxing.webdav.library.concurrent.SafeRunnable;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.log.MKLog;

import java.util.concurrent.TimeUnit;

public abstract class JackrabbitLoader extends SafeRunnable {

	protected           int    BUFFER_SIZE      = 4096;
	public final static String LOADER_FILE_TEMP = ".webdavloader";

	int mProgress;
	Handler mHandler = new Handler(Looper.getMainLooper());

	public JackrabbitLoader() {
	}

	protected abstract void safeLoad(LocalPath local, JackrabbitPath jackrabbitPath) throws Throwable;

	protected abstract LocalPath getLocalPath();

	protected abstract JackrabbitPath getJackrabbitPath();

	protected abstract Context getContext();

	@Override
	protected void onRun() throws Throwable {
		TimeUnit.MILLISECONDS.sleep(10); // For interruption
		LocalPath local = getLocalPath();
		JackrabbitPath webdavpath = getJackrabbitPath();
		if (local == null || webdavpath == null) {
			throw new IllegalArgumentException(local == null ? "Local" : "WebDAVPath" + " path is illegal.");
		}
		safeLoad(local, webdavpath);
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable t) {
		MKLog.e(JackrabbitLoader.class, "Throw: %s. Message: %s", t, t.getMessage());
		return false;
	}

	protected void onProgressUpdate(int progress) {

	}

	protected void publishProgress(final int progress) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				onProgressUpdate(progress);
			}
		});
	}

	public int getProgress() {
		return mProgress;
	}
}
