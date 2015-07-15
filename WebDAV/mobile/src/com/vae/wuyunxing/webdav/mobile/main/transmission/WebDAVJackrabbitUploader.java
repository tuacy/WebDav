package com.vae.wuyunxing.webdav.mobile.main.transmission;

import android.content.Context;

import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.transmission.JackrabbitUploader;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.util.PathUtil;
import com.vae.wuyunxing.webdav.mobile.MobileApplication;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.CancellationEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.CompletionEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FailureEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferStartEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.UpdateProgressEvent;
import com.vae.wuyunxing.webdav.mobile.storage.UploadInfoRepository;

import de.greenrobot.event.EventBus;
import greendao.UploadInfo;

public class WebDAVJackrabbitUploader extends JackrabbitUploader {

	private final UploadInfo mInfo;
	private final Context    mContext;

	public WebDAVJackrabbitUploader(Context context, UploadInfo info) {
		mInfo = info;
		mContext = context;
	}

	@Override
	protected Context getContext() {
		return mContext;
	}

	@Override
	protected LocalPath getLocalPath() {
		UploadInfo info = mInfo;
		return new LocalPath(info.getFrom(), info.getFilename());
	}

	@Override
	protected JackrabbitPath getJackrabbitPath() {
			UploadInfo info = mInfo;
		String domain = mContext.getResources().getString(R.string.webdav_domain);
		String root = mContext.getResources().getString(R.string.webdav_root);
		String password = mContext.getResources().getString(R.string.webdav_password);
			return new JackrabbitPath(domain, PathUtil.appendPath(false, info.getTo(), info.getFilename()),
									  root, password);
	}

	private void setStateAndProgress(int state, int progress) {
		mInfo.setState(state);
		mInfo.setPercent(progress);
		UploadInfoRepository.insertOrUpdate(MobileApplication.getInstance(), mInfo);
		printUploadState();
	}

	@Override
	protected void onStart() {
		setStateAndProgress(UploadInfoRepository.UPLOADING, mInfo.getPercent());

		EventBus.getDefault().post(new TransferStartEvent(true, mInfo.getId(), mInfo.getAutoSyncUpload()));
		printUploadState();
	}

	@Override
	protected void onComplete() {
		setStateAndProgress(UploadInfoRepository.FINISH, 100);

		EventBus.getDefault().post(new CompletionEvent(true, mInfo.getId(), mInfo.getFilename(), mInfo.getAutoSyncUpload()));
		printUploadState();
	}

	@Override
	protected void onCancel(Throwable t) {
		if (t instanceof InterruptedException) {
			setStateAndProgress(UploadInfoRepository.STOP, getProgress());
		} else {
			setStateAndProgress(UploadInfoRepository.READY, getProgress());
		}

		EventBus.getDefault().post(new CancellationEvent(true, mInfo.getId(), mInfo.getFilename(), mInfo.getAutoSyncUpload()));
		printUploadState();
	}

	@Override
	protected void onFailed() {
		int progress = mInfo.getPercent();
		setStateAndProgress(UploadInfoRepository.FAIL, getProgress() > progress ? getProgress() : progress);

		EventBus.getDefault().post(new FailureEvent(true, mInfo.getId(), mInfo.getFilename(), mInfo.getAutoSyncUpload()));
		printUploadState();
	}

	@Override
	protected void onProgressUpdate(int progress) {
		mInfo.setPercent(progress);
		UploadInfoRepository.insertOrUpdate(MobileApplication.getInstance(), mInfo);
		EventBus.getDefault().post(new UpdateProgressEvent(true, mInfo.getId(), progress, mInfo.getFilename(), mInfo.getAutoSyncUpload()));
		printUploadState();
	}

	// temp for debug
	private void printUploadState() {

		String state = null;
		switch (mInfo.getState()) {
			case -1:
				state = "Fail";
				break;
			case 0:
				state = "Finish";
				break;
			case 1:
				state = "Ready";
				break;
			case 2:
				state = "Ing";
				break;
			case 3:
				state = "Stop";
				break;
			default:
				break;
		}
		MKLog.d(WebDAVJackrabbitUploader.class, "---------------------------------------------");
		MKLog.d(WebDAVJackrabbitUploader.class, "ID               : " + mInfo.getId());
		MKLog.d(WebDAVJackrabbitUploader.class, "getFilename      : " + mInfo.getFilename());
		MKLog.d(WebDAVJackrabbitUploader.class, "getFrom          : " + mInfo.getFrom());
		MKLog.d(WebDAVJackrabbitUploader.class, "getTo            : " + mInfo.getTo());
		MKLog.d(WebDAVJackrabbitUploader.class, "state            : " + state);
		MKLog.d(WebDAVJackrabbitUploader.class, "progress         : " + mInfo.getPercent());
		MKLog.d(WebDAVJackrabbitUploader.class, "AutoSyncDownload : " + mInfo.getAutoSyncUpload());
	}
}
