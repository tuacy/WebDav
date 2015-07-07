package com.vae.wuyunxing.webdav.library.loader.jackrabbit.transmission;

import android.net.Uri;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.IllegalJackrabbitContentException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.IllegalJackrabbitParameterException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.IllegalLocalParameterException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.RemoteLoaderException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.RenameException;
import com.vae.wuyunxing.webdav.library.log.MKLog;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;

public abstract class JackrabbitDownloader extends JackrabbitLoader {

	private OnDatatransferProgressListener listener = new OnDatatransferProgressListener() {

		@Override
		public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName) {
			int progress = (int) (totalTransferredSoFar * 100 / totalToTransfer);
			if (100 == progress) { // when after rename then set progress = 100
				progress = 99;
			}
			mProgress = progress;
			publishProgress(mProgress);
		}
	};

	@Override
	protected void safeLoad(LocalPath local, JackrabbitPath jackrabbitPath) throws Throwable {

		long startTime, currentTime;

		// jackrabbit parameter detection
		if (null == jackrabbitPath.getBaseUrl()) {
			throw new IllegalJackrabbitParameterException("IllegalJackrabbitParameterException ......... url null");
		}
		if (null == jackrabbitPath.getUser()) {
			throw new IllegalJackrabbitParameterException("IllegalJackrabbitParameterException ......... user null");
		}
		if (null == jackrabbitPath.getPassword()) {
			throw new IllegalJackrabbitParameterException("IllegalJackrabbitParameterException ......... password null");
		}
		if (null == jackrabbitPath.getPath()) {
			throw new IllegalJackrabbitParameterException("IllegalJackrabbitParameterException ......... path null");
		}
		if (null == getContext()) {
			throw new IllegalJackrabbitContentException("IllegalJackrabbitContentException ......... content null");
		}
		// local parameter detection
		if (null == local.getFullPath()) {
			throw new IllegalLocalParameterException("IllegalLocalParameterException ......... path null");
		}
		// log cat info
		MKLog.d(JackrabbitDownloader.class, "jackrabbit domin url: %s", jackrabbitPath.getBaseUrl());
		MKLog.d(JackrabbitDownloader.class, "jackrabbit user: %s", jackrabbitPath.getUser());
		MKLog.d(JackrabbitDownloader.class, "jackrabbit password: %s", jackrabbitPath.getPassword());
		MKLog.d(JackrabbitDownloader.class, "jackrabbit local path: %s", local.getFullPath());
		MKLog.d(JackrabbitDownloader.class, "jackrabbit remoter: %s", jackrabbitPath.getPath());

		// new client
		Uri serverUri = Uri.parse(jackrabbitPath.getBaseUrl());
		OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(serverUri, getContext(), true);
		client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(jackrabbitPath.getUser(), jackrabbitPath.getPassword()));

		// get method (put webdav path)
		GetMethod get = new GetMethod(client.getWebdavUri() + WebdavUtils.encodePath(jackrabbitPath.getPath()));

		// local file (mkdirs parent)
		File localFile = new File(local.getFullPath());
		localFile.getParentFile().mkdirs();
		// temp file
		// temp file
		File localTmpFile = new File(local.getFullPath() + LOADER_FILE_TEMP);
		if (localTmpFile.exists()) {
			localTmpFile.delete();
		}

		// file stream
		FileOutputStream localFileOutputStream = null;

		int status = 0;
		try {
			// download the stream
			status = client.executeMethod(get);
			if (isSuccess(status)) {
				localTmpFile.createNewFile();
				BufferedInputStream webdavFileBufferInputStream = new BufferedInputStream(get.getResponseBodyAsStream());
				localFileOutputStream = new FileOutputStream(localTmpFile);
				// file length
				Header contentLength = get.getResponseHeader("Content-Length");
				// total transfer length
				long totalToTransfer = (contentLength != null && contentLength.getValue().length() > 0) ?
									   Long.parseLong(contentLength.getValue()) : 0;
				// transfer
				byte[] bytes = new byte[BUFFER_SIZE];
				int readLength = 0;
				long transferredLength = 0;
				startTime = System.currentTimeMillis();
				while ((readLength = webdavFileBufferInputStream.read(bytes)) != -1) {
					localFileOutputStream.write(bytes, 0, readLength);
					transferredLength += readLength;
					currentTime = System.currentTimeMillis();
					if (currentTime - startTime > 1000) {
						startTime = currentTime;
						listener.onTransferProgress(readLength, transferredLength, totalToTransfer, localFile.getName());
					}
				}
				// trans completed
				if (transferredLength != totalToTransfer) {
					if (localTmpFile.exists()) {
						localTmpFile.delete();
					}
					client.exhaustResponse(get.getResponseBodyAsStream());
				}
			} else {
				client.exhaustResponse(get.getResponseBodyAsStream());
			}
		} finally {
			if (localFileOutputStream != null) {
				localFileOutputStream.close();
			}
			get.releaseConnection();    // let the connection available for other methods
		}

		// get the operation result
		RemoteOperationResult result = new RemoteOperationResult(isSuccess(status), status, get.getResponseHeaders());
		if (result.getCode() == RemoteOperationResult.ResultCode.OK) {
			if (localTmpFile.renameTo(localFile)) {
				mProgress = 100;
				publishProgress(mProgress);
			} else {
				throw new RenameException("Download down but rename error");
			}
		} else {
			throw new RemoteLoaderException("JackrabbitDownloader error");
		}
	}


	private boolean isSuccess(int status) {
		return (status == HttpStatus.SC_OK);
	}
}
