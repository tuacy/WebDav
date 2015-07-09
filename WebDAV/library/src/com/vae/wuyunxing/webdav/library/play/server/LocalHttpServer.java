package com.vae.wuyunxing.webdav.library.play.server;

import android.util.Log;


import com.vae.wuyunxing.webdav.library.play.webdav.WebDAVFilePlayService;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalHttpServer {

	private static final String TAG = LocalHttpServer.class.getSimpleName();

	private RequestListenerThread mReqListener;

	public LocalHttpServer(String addr, int port, HttpRequestHandler handler) throws IOException {
		mReqListener = new RequestListenerThread(addr, port, handler);
	}

	public void execute() {
		mReqListener.setDaemon(false);
		mReqListener.start();
	}

	public void stop() {
		mReqListener.interrupt();
	}

	private static final class RequestListenerThread extends Thread {

		private static final int SO_TIMEOUT         = 5000;
		private static final int SOCKET_BUFFER_SIZE = 8 * 1024;

		private final ServerSocket mServerSocket;
		private final HttpParams   mParams;
		private final HttpService  mHttpService;

		private ExecutorService mThreadPool;

		public RequestListenerThread(String addr, int port, HttpRequestHandler handler) throws IOException {
			InetAddress bindAddr = InetAddress.getByName(addr);
			mServerSocket = new ServerSocket(port, 0, bindAddr);

			HttpProcessor httpProc = new ImmutableHttpProcessor(new ResponseDate(), new ResponseServer(), new ResponseContent(),
																new ResponseConnControl());

			mParams = new BasicHttpParams();
			mParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, SO_TIMEOUT)
				   .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, SOCKET_BUFFER_SIZE)
				   .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
				   .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
				   .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");

			HttpRequestHandlerRegistry registry = new HttpRequestHandlerRegistry();
			registry.register("*", handler);

			mHttpService = new HttpService(httpProc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
			mHttpService.setParams(mParams);

			mHttpService.setHandlerResolver(registry);

			mThreadPool = Executors.newCachedThreadPool();


			/** for WebDAV file play get address and port */
			WebDAVFilePlayService.IP = bindAddr.getHostAddress();
			WebDAVFilePlayService.PORT = port;
		}

		@Override
		public void run() {
			while (!isInterrupted()) {
				try {
					Socket socket = mServerSocket.accept();
					final DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
					conn.bind(socket, mParams);
					mThreadPool.execute(new Runnable() {
						@Override
						public void run() {
							while (!isInterrupted() && conn.isOpen()) {
								try {
									mHttpService.handleRequest(conn, new BasicHttpContext());
								} catch (IOException e) {
								} catch (HttpException e) {
								} finally {
									try {
										conn.shutdown();
									} catch (IOException ignored) {
									}
								}
							}
						}
					});
				} catch (IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}

			mThreadPool.shutdownNow();
		}
	}
}
