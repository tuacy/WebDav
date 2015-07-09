package com.vae.wuyunxing.webdav.library.play.webdav;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.resources.files.RemoteFile;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitFileExplorer;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.play.server.HostInterface;
import com.vae.wuyunxing.webdav.library.play.server.LocalHttpServer;
import com.vae.wuyunxing.webdav.library.play.util.Utils;
import com.vae.wuyunxing.webdav.library.remotefileoperations.jackrabbit.JackrabbitRemoteFileOperations;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * test code
 */
//{
//
//	/** http request url */
//	String httpRequest = "http://" + WebDAVFilePlayService.IP + ':' + WebDAVFilePlayService.PORT + WebDAVFileServer.WEBDAV_CONTENT_EXPORT_URI;
//	/** get router ip(WebDAVFileServer need it) */
//	RouterManager routerManager = MobileApplication.getInstance().getRouterManager();
//	String routerIP = routerManager.getRouterIP();
//	/** WebDAV file path */
//	String WebDAVFileCurrentPath = "/1.mp3";
//	String ext = Utils.getFileExtension(WebDAVFileCurrentPath);
//	String WebDAVFileCurrentEncodePath = null;
//	try {
//	WebDAVFileCurrentEncodePath = URLEncoder.encode(WebDAVFileCurrentPath, "UTF-8");
//	} catch (UnsupportedEncodingException e) {
//	e.printStackTrace();
//	}
//
//	String url = httpRequest + routerIP + WebDAVFileCurrentEncodePath;
//
//	Intent intent = new Intent(Intent.ACTION_VIEW);
//	Uri uri = Uri.parse(url);
//	intent.setDataAndType(uri, MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext));
//	startActivity(intent);
//	}

/**
 * WebDAV file play
 */
public class WebDAVFileServer extends Thread implements HttpRequestHandler {

	private static final String TAG = WebDAVFileServer.class.getSimpleName();

	/** WebDAV file play tag */
	public static final String WEBDAV_CONTENT_EXPORT_URI = "/webdav=";

	private static final int INIT_PORT = 2222;

	private static final int RETRY_COUNT = 5;

	private LocalHttpServer mHttpServer;

	private Context mContext;

	public WebDAVFileServer(Context context) {
		mContext = context;
	}

	@Override
	public void run() {
		int port = INIT_PORT;
		String ipAddr = "";

		/** get address */
		int n = HostInterface.getNHostAddresses();
		if (n >= 1) {
			ipAddr = HostInterface.getHostAddress(0);
		}

		/** get port */
		for (int i = 0; i < RETRY_COUNT; i++) {
			if (Utils.checkPort(ipAddr, port)) {
				break;
			}
			port++;
		}

		try {
			mHttpServer = new LocalHttpServer(ipAddr, port, this);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return;
		}
		mHttpServer.execute();
	}

	public LocalHttpServer getHttpServer() {
		return mHttpServer;
	}

	@Override
	public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
		/** get the url ex: /webdav=192.168.11.1%2F1.mp3 */
		String uri = httpRequest.getRequestLine().getUri();

		/** check is WebDAV file */
		if (!uri.startsWith(WEBDAV_CONTENT_EXPORT_URI)) {
			httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			return;
		}

		try {
			/** get the url ex: /webdav=192.168.11.1/1.mp3 */
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, e.getMessage());
		}

		/** get the domain and WebDAV file path (ex: /1.mp3 ) */
		String tempString = uri.substring(WEBDAV_CONTENT_EXPORT_URI.length()); /** ex:tempString = "192.168.11.1/1.mp3"  */
		int firstSeparateIndex = tempString.indexOf("/");
		String domain = tempString.substring(0, firstSeparateIndex);
		String WebDAVFilePath = tempString.substring(firstSeparateIndex);

		/** get user(root) and password(admin) */
		String user = "root"; // root
		String password = "admin"; // admin

		/** JackrabbitPath */
		JackrabbitPath jackrabbitPath = new JackrabbitPath(domain, WebDAVFilePath, user, password);

		/** get WebDAV file length */
		long contentLength = 0;
		OwnCloudClient client = OwnCloudClientFactory.createOwnCloudClient(Uri.parse(jackrabbitPath.getBaseUrl()), mContext, true);
		client.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(jackrabbitPath.getUser(), jackrabbitPath.getPassword()));
		RemoteFile remoteFile = JackrabbitFileExplorer.executeGetFile(client, WebDAVFilePath, false);
		/** get the file content error then return */
		if (remoteFile == null) {
			httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			return;
		}
		contentLength = remoteFile.getLength();
		/** file content length = 0 return */
		if (contentLength <= 0) {
			httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			return;
		}

		/** get WebDAV file stream */
		JackrabbitRemoteFileOperations jackrabbitRemoteOperations = new JackrabbitRemoteFileOperations(mContext);
		InputStream inputStream = jackrabbitRemoteOperations.getStream(jackrabbitPath);
		if (null == inputStream) {
			httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
			return;
		}

		/** get WebDAV file content type */
		ContentType contentType = Utils.getContentType(WebDAVFilePath);

		WebDAVFileEntity entity = new WebDAVFileEntity(inputStream, contentLength, contentType);
		entity.setChunked(true);

		httpResponse.setStatusCode(HttpStatus.SC_OK);
		httpResponse.setEntity(entity);
	}
}
