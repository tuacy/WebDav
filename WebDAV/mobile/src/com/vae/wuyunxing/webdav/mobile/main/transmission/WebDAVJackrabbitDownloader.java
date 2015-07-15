package com.vae.wuyunxing.webdav.mobile.main.transmission;

import android.content.Context;


import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.transmission.JackrabbitDownloader;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.util.PathUtil;
import com.vae.wuyunxing.webdav.mobile.MobileApplication;
import com.vae.wuyunxing.webdav.mobile.R;
import com.vae.wuyunxing.webdav.mobile.main.message.CancellationEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.CompletionEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.FailureEvent;
import com.vae.wuyunxing.webdav.mobile.main.transfer.message.TransferStartEvent;
import com.vae.wuyunxing.webdav.mobile.main.message.UpdateProgressEvent;
import com.vae.wuyunxing.webdav.mobile.storage.DownloadInfoRepository;

import de.greenrobot.event.EventBus;
import greendao.DownloadInfo;

public class WebDAVJackrabbitDownloader extends JackrabbitDownloader {

	private final DownloadInfo mInfo;
	private       Context      mContext;

	public WebDAVJackrabbitDownloader(Context context, DownloadInfo info) {
		mInfo = info;
		mContext = context;
	}

	@Override
	protected Context getContext() {
		return mContext;
	}

	@Override
	protected LocalPath getLocalPath() {
		DownloadInfo info = mInfo;
		return new LocalPath(info.getTo(), info.getFilename());
	}

	@Override
	protected JackrabbitPath getJackrabbitPath() {
        DownloadInfo info = mInfo;
		String domain = mContext.getResources().getString(R.string.webdav_domain);
		String root = mContext.getResources().getString(R.string.webdav_root);
		String password = mContext.getResources().getString(R.string.webdav_password);
        return new JackrabbitPath(domain, PathUtil.appendPath(false, info.getFrom(), info.getFilename()),
								  root, password);
	}

	private void setStateAndProgress(int state, int progress) {
		mInfo.setState(state);
		mInfo.setPercent(progress);

		DownloadInfoRepository.insertOrUpdate(MobileApplication.getInstance(), mInfo);
		printDownloadState();
	}

	@Override
	protected void onStart() {
		setStateAndProgress(DownloadInfoRepository.DOWNLOADING, mInfo.getPercent());

		EventBus.getDefault().post(new TransferStartEvent(false, mInfo.getId(), mInfo.getAutoSyncDownload()));
		printDownloadState();
	}

	@Override
	protected void onComplete() {
		setStateAndProgress(DownloadInfoRepository.FINISH, 100);

		EventBus.getDefault().post(new CompletionEvent(false, mInfo.getId(), mInfo.getFilename(), mInfo.getAutoSyncDownload()));
		printDownloadState();
	}

	@Override
	protected void onCancel(Throwable t) {
		if (t instanceof InterruptedException) {
			setStateAndProgress(DownloadInfoRepository.STOP, getProgress());
		} else {
			setStateAndProgress(DownloadInfoRepository.READY, getProgress());
		}

		EventBus.getDefault().post(new CancellationEvent(false, mInfo.getId(), mInfo.getFilename(), mInfo.getAutoSyncDownload()));
		printDownloadState();
	}

	@Override
	protected void onFailed() {
		int progress = mInfo.getPercent();
		setStateAndProgress(DownloadInfoRepository.FAIL, getProgress() > progress ? getProgress() : progress);

		EventBus.getDefault().post(new FailureEvent(false, mInfo.getId(), mInfo.getFilename(), mInfo.getAutoSyncDownload()));
		printDownloadState();
	}

	@Override
	protected void onProgressUpdate(int progress) {
		mInfo.setPercent(progress);
		DownloadInfoRepository.insertOrUpdate(MobileApplication.getInstance(), mInfo);
		EventBus.getDefault()
				.post(new UpdateProgressEvent(false, mInfo.getId(), progress, mInfo.getFilename(), mInfo.getAutoSyncDownload()));
		printDownloadState();
	}

	// temp for debug
	private void printDownloadState() {

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
		MKLog.d(WebDAVJackrabbitDownloader.class, "---------------------------------------------");
		MKLog.d(WebDAVJackrabbitDownloader.class, "ID               : " + mInfo.getId());
		MKLog.d(WebDAVJackrabbitDownloader.class, "getFilename      : " + mInfo.getFilename());
		MKLog.d(WebDAVJackrabbitDownloader.class, "getFrom          : " + mInfo.getFrom());
		MKLog.d(WebDAVJackrabbitDownloader.class, "getTo            : " + mInfo.getTo());
		MKLog.d(WebDAVJackrabbitDownloader.class, "state            : " + state);
		MKLog.d(WebDAVJackrabbitDownloader.class, "progress         : " + mInfo.getPercent());
		MKLog.d(WebDAVJackrabbitDownloader.class, "AutoSyncDownload : " + mInfo.getAutoSyncDownload());
	}
}
