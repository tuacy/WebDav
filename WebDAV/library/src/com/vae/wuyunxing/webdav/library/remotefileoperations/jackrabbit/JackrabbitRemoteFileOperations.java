package com.vae.wuyunxing.webdav.library.remotefileoperations.jackrabbit;

import android.content.Context;
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
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;
import com.vae.wuyunxing.webdav.library.loader.jackrabbit.MimeTypeManager;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.remotefileoperations.RemoteFileOperations;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//Task.callInBackground(new Callable<Void>() {
//@Override
//public Void call() throws Exception {
//	JackrabbitRemoteFileOperations webdavuploadtest = new JackrabbitRemoteFileOperations(mContext);
//	LocalPath localPath = new LocalPath(Environment.getExternalStorageDirectory().toString(), "124.mp3");
//	JackrabbitPath jackrabbitPath = new JackrabbitPath("192.168.11.1", "/1233333.mp3", "root", "admin");
//	if (webdavuploadtest.put(jackrabbitPath, localPath)) {
//	Log.d("vae_tag", "put success");
//	} else {
//	Log.d("vae_tag", "put error");
//	}
//
//	Log.d("vae_tag", "kkkkkkkkk");
//	JackrabbitPath jackrabbitPathWrite = new JackrabbitPath("192.168.11.1", "/1123.txt", "root", "admin");
//	if (webdavuploadtest.write(jackrabbitPathWrite, "aaa" + mContext.getResources().getString(R.string.about))) {
//	Log.d("vae_tag", "wrtite success");
//	} else {
//	Log.d("vae_tag", "write success");
//	}
//
//	JackrabbitPath jackrabbitPathGet = new JackrabbitPath("192.168.11.1", "/123.mp3", "root", "admin");
//	LocalPath localPathGet = new LocalPath(Environment.getExternalStorageDirectory().toString(), "125.mp3");
//	if (webdavuploadtest.get(jackrabbitPathGet, localPathGet)) {
//	Log.d("vae_tag", "get success");
//	} else {
//	Log.d("vae_tag", "get success");
//	}
//
//	JackrabbitPath jackrabbitPathdelete = new JackrabbitPath("192.168.11.1", "/123.mp3", "root", "admin");
//	List<JackrabbitPath> list = new ArrayList<JackrabbitPath>();
//	list.add(jackrabbitPathdelete);
//	webdavuploadtest.delete(list);
//	return null;
//	}
//	});

public class JackrabbitRemoteFileOperations implements RemoteFileOperations<JackrabbitPath, LocalPath> {

	private Context mContext;

	public JackrabbitRemoteFileOperations(Context context) {
		mContext = context;
	}

	@Override
	public boolean put(JackrabbitPath jackrabbitPath, LocalPath localPath) {
		// log cat info
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit domin url: %s", jackrabbitPath.getBaseUrl());
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit user: %s", jackrabbitPath.getUser());
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit password: %s", jackrabbitPath.getPassword());
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit local path: %s", localPath.getFullPath());
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit remoter: %s", jackrabbitPath.getPath());

		try {
			// new client
			Uri serverUri = Uri.parse(jackrabbitPath.getBaseUrl());
			OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
			client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(jackrabbitPath.getUser(), jackrabbitPath.getPassword()));

			// mkdirs
			try {
				RemoteOperation operation = new CreateRemoteFolderOperation(FileUtils.getParentPath(jackrabbitPath.getPath()), true);
				RemoteOperationResult result = operation.execute(client);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// put method (put webdav path)
			PutMethod putMethod = new PutMethod(client.getWebdavUri() + WebdavUtils.encodePath(jackrabbitPath.getPath()));

			// get local file stream to RequestEntity and set progress listener
			File localFile = new File(localPath.getFullPath());
			String mimeType = MimeTypeManager.getMimeTypeString(localPath.getFullPath());
			MKLog.d(JackrabbitRemoteFileOperations.class, "current file mimeType: %s", mimeType);
			RequestEntity entity = new FileRequestEntity(localFile, mimeType);
			Set<OnDatatransferProgressListener> dataTransferListeners = new HashSet<OnDatatransferProgressListener>();
			((ProgressiveDataTransferer) entity).addDatatransferProgressListeners(dataTransferListeners);
			putMethod.setRequestEntity(entity);
			// upload
			int status = 0;
			status = client.executeMethod(putMethod);
			client.exhaustResponse(putMethod.getResponseBodyAsStream());
			// get the result
			RemoteOperationResult result = new RemoteOperationResult(isSuccess(status), status, putMethod.getResponseHeaders());
			if (result.getCode() == RemoteOperationResult.ResultCode.OK) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean write(JackrabbitPath jackrabbitPath, String info) {
		// log cat info
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit domin url: %s", jackrabbitPath.getBaseUrl());
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit user: %s", jackrabbitPath.getUser());
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit password: %s", jackrabbitPath.getPassword());
		MKLog.d(JackrabbitRemoteFileOperations.class, "jackrabbit remoter: %s", jackrabbitPath.getPath());

		try {
			// new client
			Uri serverUri = Uri.parse(jackrabbitPath.getBaseUrl());
			OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
			client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(jackrabbitPath.getUser(), jackrabbitPath.getPassword()));

			// mkdirs
			try {
				RemoteOperation operation = new CreateRemoteFolderOperation(FileUtils.getParentPath(jackrabbitPath.getPath()), true);
				RemoteOperationResult result = operation.execute(client);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// put method (put webdav path)
			PutMethod putMethod = new PutMethod(client.getWebdavUri() + WebdavUtils.encodePath(jackrabbitPath.getPath()));

			RequestEntity entity = new StringRequestEntity(info, "text/plain", "utf-8");
			putMethod.setRequestEntity(entity);
			int status = client.executeMethod(putMethod);
			client.exhaustResponse(putMethod.getResponseBodyAsStream());
			// get the result
			RemoteOperationResult result = new RemoteOperationResult(isSuccess(status), status, putMethod.getResponseHeaders());
			if (result.getCode() == RemoteOperationResult.ResultCode.OK) {
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean get(JackrabbitPath jackrabbitPath, LocalPath localPath) {

		FileOutputStream localFileOutputStream = null;
		GetMethod get = null;
		try {
			// new client
			Uri serverUri = Uri.parse(jackrabbitPath.getBaseUrl());
			OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
			client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(jackrabbitPath.getUser(), jackrabbitPath.getPassword()));

			// get method (put webdav path)
			get = new GetMethod(client.getWebdavUri() + WebdavUtils.encodePath(jackrabbitPath.getPath()));

			// local file (mkdirs parent)
			File localFile = new File(localPath.getFullPath());
			localFile.getParentFile().mkdirs();
			// file stream

			int status = 0;

			// download the stream
			status = client.executeMethod(get);
			if (isSuccess(status)) {
				localFile.createNewFile();
				BufferedInputStream webdavFileBufferInputStream = new BufferedInputStream(get.getResponseBodyAsStream());
				localFileOutputStream = new FileOutputStream(localFile);
				// file length
				Header contentLength = get.getResponseHeader("Content-Length");
				// total transfer length
				long totalToTransfer = (contentLength != null && contentLength.getValue().length() > 0) ?
									   Long.parseLong(contentLength.getValue()) : 0;
				// transfer
				byte[] bytes = new byte[4096];
				int readLength = 0;
				long transferredLength = 0;
				while ((readLength = webdavFileBufferInputStream.read(bytes)) != -1) {
					localFileOutputStream.write(bytes, 0, readLength);
					transferredLength += readLength;
				}
				// trans completed
				if (transferredLength != totalToTransfer) {
					if (localFile.exists()) {
						localFile.delete();
					}
					client.exhaustResponse(get.getResponseBodyAsStream());
				}
				RemoteOperationResult result = new RemoteOperationResult(isSuccess(status), status, get.getResponseHeaders());
				if (result.getCode() == RemoteOperationResult.ResultCode.OK) {
					return true;
				}
			} else {
				client.exhaustResponse(get.getResponseBodyAsStream());
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (localFileOutputStream != null) {
				try {
					localFileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			get.releaseConnection();    // let the connection available for other methods
		}
		return false;
	}

	@Override
	public void delete(List<JackrabbitPath> list) {

		// new client
		if (null == list || 0 == list.size()) {
			return;
		}
		Uri serverUri = Uri.parse(list.get(0).getBaseUrl());
		OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
		client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(list.get(0).getUser(), list.get(0).getPassword()));

		for (int index = 0; index < list.size(); index++) {
			RemoveRemoteFileOperation removeOperation = new RemoveRemoteFileOperation(list.get(0).getPath());
			removeOperation.execute(client);
		}
	}

	@Override
	public InputStream getStream(JackrabbitPath jackrabbitPath) {
		GetMethod get = null;
		try {
			// new client
			Uri serverUri = Uri.parse(jackrabbitPath.getBaseUrl());
			OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(serverUri, mContext, true);
			client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(jackrabbitPath.getUser(), jackrabbitPath.getPassword()));

			// get method (put webdav path)
			get = new GetMethod(client.getWebdavUri() + WebdavUtils.encodePath(jackrabbitPath.getPath()));
			int status = 0;

			// download the stream
			status = client.executeMethod(get);
			if (isSuccess(status)) {
				return get.getResponseBodyAsStream();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private boolean isSuccess(int status) {
		return ((status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT));
	}
}
