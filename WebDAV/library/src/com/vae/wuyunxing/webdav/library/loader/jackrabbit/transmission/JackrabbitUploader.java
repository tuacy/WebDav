package com.vae.wuyunxing.webdav.library.loader.jackrabbit.transmission;

import android.net.Uri;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.FileRequestEntity;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.network.ProgressiveDataTransferer;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.RemoveRemoteFileOperation;
import com.owncloud.android.lib.resources.files.RenameRemoteFileOperation;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.MimeTypeManager;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.IllegalJackrabbitContentException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.IllegalJackrabbitParameterException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.IllegalLocalParameterException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.RemoteLoaderException;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.exception.RenameException;
import com.vae.wuyunxing.webdav.library.log.MKLog;

import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.http.HttpStatus;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * upload the file to WebDAV service(cover the original file)
 */
public abstract class JackrabbitUploader extends JackrabbitLoader {


	private OnDatatransferProgressListener listener = new OnDatatransferProgressListener() {

		private long mStartTime, mCurrentTime;

		@Override
		public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileAbsoluteName) {
			mCurrentTime = System.currentTimeMillis();
			if (mCurrentTime - mStartTime > 1000) {
				mStartTime = mCurrentTime;
				int progress = (int) (totalTransferredSoFar * 100 / totalToTransfer);
				if (100 == progress) { /** after rename then set progress = 100 */
					progress = 99;
				}
				mProgress = progress;
				publishProgress(mProgress);
			}
		}
	};

	@Override
	protected void safeLoad(LocalPath local, JackrabbitPath jackrabbitPath) throws Throwable {

		/** jackrabbit parameter detection */
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
		/** local parameter detection */
		if (null == local.getFullPath()) {
			throw new IllegalLocalParameterException("IllegalLocalParameterException ......... path null");
		}
		/** log cat info */
		MKLog.d(JackrabbitUploader.class, "jackrabbit domin url: %s", jackrabbitPath.getBaseUrl());
		MKLog.d(JackrabbitUploader.class, "jackrabbit user: %s", jackrabbitPath.getUser());
		MKLog.d(JackrabbitUploader.class, "jackrabbit password: %s", jackrabbitPath.getPassword());
		MKLog.d(JackrabbitUploader.class, "jackrabbit local path: %s", local.getFullPath());
		MKLog.d(JackrabbitUploader.class, "jackrabbit remoter: %s", jackrabbitPath.getPath());

		String tempPath = jackrabbitPath.getPath() + LOADER_FILE_TEMP;

		String tempPathFileName = new File(tempPath).getName();
		String destinationFileName = new File(jackrabbitPath.getPath()).getName();

		/** new client */
		Uri serverUri = Uri.parse(jackrabbitPath.getBaseUrl());
		OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(serverUri, getContext(), true);
		client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(jackrabbitPath.getUser(), jackrabbitPath.getPassword()));

		/** put method (put webdav path) */
		PutMethod putMethod = new PutMethod(client.getWebdavUri() + WebdavUtils.encodePath(tempPath));

		/** mkdirs */
		try {
			RemoteOperation operation = new CreateRemoteFolderOperation(FileUtils.getParentPath(jackrabbitPath.getPath()), true);
			/** no need check the result (if the file exit the result will not success) */
			operation.execute(client);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/** get local file stream to RequestEntity and set progress listener */
		File localFile = new File(local.getFullPath());
		String mimeType = MimeTypeManager.getMimeTypeString(local.getFullPath());
		MKLog.d(JackrabbitUploader.class, "current file mimeType: %s", mimeType);
		RequestEntity entity = new FileRequestEntity(localFile, mimeType);
		Set<OnDatatransferProgressListener> dataTransferListeners = new HashSet<OnDatatransferProgressListener>();
		dataTransferListeners.add(listener);
		((ProgressiveDataTransferer) entity).addDatatransferProgressListeners(dataTransferListeners);
		putMethod.setRequestEntity(entity);

		/** upload */
		int status = client.executeMethod(putMethod);
		client.exhaustResponse(putMethod.getResponseBodyAsStream());
		putMethod.releaseConnection();

		/** get the result */
		RemoteOperationResult result = new RemoteOperationResult(isSuccess(status), status, putMethod.getResponseHeaders());
		if (result.getCode() == RemoteOperationResult.ResultCode.OK) {

			/** rename (temp file name to real file name) */
			RenameRemoteFileOperation renameFileOperation = new RenameRemoteFileOperation(tempPathFileName, tempPath, destinationFileName,
																						  false);
			renameFileOperation.setCoverState(true);
			RemoteOperationResult renameResult = renameFileOperation.execute(client);
			if (RemoteOperationResult.ResultCode.OK == renameResult.getCode() ||
				RemoteOperationResult.ResultCode.INVALID_OVERWRITE == renameResult.getCode()) {
				if (RemoteOperationResult.ResultCode.INVALID_OVERWRITE == renameResult.getCode()) {
					/** destination file exist before so rename error delete temp file */
					RemoveRemoteFileOperation removeRemoteFileOperation = new RemoveRemoteFileOperation(tempPath);
					removeRemoteFileOperation.execute(client);
				}
				mProgress = 100;
				publishProgress(mProgress);
			} else {
				RemoveRemoteFileOperation removeRemoteFileOperation = new RemoveRemoteFileOperation(tempPath);
				removeRemoteFileOperation.execute(client);
				throw new RenameException("Upload down but rename error and the error code = " + renameResult.getCode());
			}
		} else {
			throw new RemoteLoaderException("JackrabbitUploader error");
		}
	}

	private boolean isSuccess(int status) {
		return ((status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT));
	}


}
