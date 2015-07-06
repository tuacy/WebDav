package com.vae.wuyunxing.webdav.library.util;

import android.net.Uri;

import java.io.File;

public class PathUtil {

	public static String getLastComponent(String path) {
		String[] array = path.split("/");
		return array[array.length - 1];
	}

	public static String retrieve(String base, String to) {
		StringBuilder builder = new StringBuilder(base);
		if (builder.charAt(builder.length() - 1) != '/') {
			builder.append('/');
		}

		String[] components = to.split("/");
		for (String s : components) {
			if (s.equals("..")) {
				int length = builder.length();
				while (builder.charAt(length - 1) == '/' && length >= 0) {
					builder.deleteCharAt(length - 1);
					length = builder.length();
				}
				builder.delete(builder.lastIndexOf("/") + 1, builder.length());
			} else if (!s.equals("") && !s.equals(".")) {
				builder.append(s).append('/');
			}
		}

		/** if base not directory then delete '/' */
		if (!to.endsWith("/") && !to.equals("..") && !to.equals(".")) {
			builder.deleteCharAt(builder.length() - 1);
		}
		return builder.toString();
	}

	public static String getNameFromPath(String path) {
		if (path == null || path.length() < 2) {
			return null;
		}
		int slash = path.lastIndexOf('/');
		if (slash == -1) {
			return path;
		}
		return path.substring(slash + 1);
	}

	public static String appendPath(boolean dir, String path, String... params) {
		StringBuilder builder = new StringBuilder(path);
		int lastStrIndex = builder.length() - 1;
		if (builder.charAt(lastStrIndex) == '/') {
			builder.deleteCharAt(lastStrIndex);
		}

		for (int i = 0; i < params.length; i++) {
			String param = params[i];
			if (param.startsWith(File.separator)) {
				builder.append(param);
			} else {
				builder.append(File.separator).append(param);
			}
			lastStrIndex = builder.length() - 1;
			if (builder.charAt(lastStrIndex) == '/') {
				builder.deleteCharAt(lastStrIndex);
			}
		}

		if (dir) {
			builder.append(File.separator);
		}

		return builder.toString();
	}

	public static String encodePath(String path) {
		String encodedPath = Uri.encode(path, "/");
		if (!encodedPath.startsWith("/")) {
			encodedPath = "/" + encodedPath;
		}
		return encodedPath;
	}
}
