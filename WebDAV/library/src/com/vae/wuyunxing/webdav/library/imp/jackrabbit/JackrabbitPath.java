package com.vae.wuyunxing.webdav.library.imp.jackrabbit;

import com.vae.wuyunxing.webdav.library.config.LibraryConfig;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.UsernamePasswordCredentials;

import java.io.File;

public class JackrabbitPath {

	public static final String MOUNT_DIR = LibraryConfig.getInstance().getString(LibraryConfig.WEBDAV_ROOT_PATH);
	public static final String PORT      = "8080";
	private String mHost;
	private String mPath;
	private String mUser;
	private String mPassword;

	public JackrabbitPath(String host, String path, String user, String password) {
		mHost = host;
		//eg:http://192.168.11.1:8080/home/HomeSharing/image, path shoule be: home/HomeSharing/image
		mPath = path.startsWith(File.separator) ? path : (File.separator + path);
		mUser = user;
		mPassword = password;
	}

	public Credentials getCredentials() {
		return new UsernamePasswordCredentials(mUser, mPassword);
	}

	public String getUrl() {
		return "http://" + mHost + ":" + PORT + mPath;
	}

	public String getBaseUrl() {
		return "http://" + mHost + ":" + PORT;
	}

	public void setHost(String domain) {
		mHost = domain;
	}

	public void setPath(String path) {
		mPath = path;
	}

	public void setUser(String user) {
		mUser = user;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public String getPath() {
		return mPath;
	}

	public String getUser() {
		return mUser;
	}

	public String getPassword() {
		return mPassword;
	}

	public String getHost() {
		return mHost;
	}
}
