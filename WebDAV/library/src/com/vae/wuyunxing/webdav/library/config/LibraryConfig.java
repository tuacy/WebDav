package com.vae.wuyunxing.webdav.library.config;

import android.content.Context;
import android.content.res.AssetManager;

import com.vae.wuyunxing.webdav.library.log.MKLog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LibraryConfig {

	public static final String WEBDAV_ROOT_PATH    = "WEBDAV_ROOT_PATH";

	private static final Properties mProps = new Properties();
	private static LibraryConfig sInstance;

	public static void initialize(Context context) {
		InputStream in = null;
		try {
			AssetManager assetManager = context.getAssets();
			in = assetManager.open("library-configs.properties");
			mProps.load(in);
		} catch (IOException e) {
			MKLog.e(LibraryConfig.class, e, "Connot open: %s", "library-configs.properties");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignored) {
				}
			}
		}
	}

	public synchronized static LibraryConfig getInstance() {
		if (sInstance == null) {
			sInstance = new LibraryConfig();
		}
		return sInstance;
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defVal) {
		return Boolean.parseBoolean(mProps.getProperty(key, Boolean.toString(defVal)));
	}

	public int getInteger(String key) {
		return getInteger(key, 0);
	}

	public int getInteger(String key, int defVal) {
		return Integer.parseInt(mProps.getProperty(key, Integer.toString(defVal)));
	}

	public String getString(String key, String defaultValue) {
		return mProps.getProperty(key, defaultValue);
	}

	public String getString(String key) {
		return mProps.getProperty(key);
	}
}
