package com.vae.wuyunxing.webdav.library.loader.jackrabbit.test;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.transmission.JackrabbitUploader;

//		Task.callInBackground(new Callable<Void>() {
//			@Override
//			public Void call() throws Exception {
//				JackrabbitUploaderText webdavuploadtest = new JackrabbitUploaderText(mContext);
//				webdavuploadtest.run();
//				JackrabbitDownloaderText webdavdownlaoderest = new JackrabbitDownloaderText(mContext);
//				webdavdownlaoderest.run();
//				return null;
//			}
//		});

public class JackrabbitUploaderText extends JackrabbitUploader {

	private Context mContext;

	public JackrabbitUploaderText(Context context) {
		mContext = context;
	}

	@Override
	public Context getContext() {
		return mContext;
	}

	@Override
	protected LocalPath getLocalPath() {
		return new LocalPath(Environment.getExternalStorageDirectory().toString(), "DCIM/Camera//IMG_20141005_175447.jpg");
	}

	@Override
	protected JackrabbitPath getJackrabbitPath() {
		return new JackrabbitPath("192.168.11.1", JackrabbitPath.MOUNT_DIR + "/93749c14ff7930ca79c4e16aae867e80.jpg", "root", "admin");
	}

	@Override
	protected void onProgressUpdate(int progress) {
		Log.d("vae_tag", "progress = " + progress);
	}

	@Override
	protected void onComplete() {
		Log.d("vae_tag", "uploader commplete");
	}

	@Override
	protected void onFailed() {
		Log.d("vae_tag", "uploader onFailed");
	}
}
