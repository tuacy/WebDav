package com.vae.wuyunxing.webdav.library.play.webdav;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class WebDAVFilePlayService extends Service {


	private WebDAVFileServer mWebDAVFileServer;
	/** will get the IP at LocalHttpServer */
	public static String IP   = "";
	/** will get the PORT at LocalHttpServer */
	public static int    PORT = 0;

	/** start server */
	public static void startup(Context context) {
		Intent i = new Intent(context, WebDAVFilePlayService.class);
		context.startService(i);
	}

	/** stop server */
	public static void stopdown(Context context) {
		Intent i = new Intent(context, WebDAVFilePlayService.class);
		context.stopService(i);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		mWebDAVFileServer = new WebDAVFileServer(this);
		mWebDAVFileServer.start();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mWebDAVFileServer.getHttpServer().stop();
	}
}
