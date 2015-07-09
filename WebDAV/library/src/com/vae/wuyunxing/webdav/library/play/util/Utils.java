package com.vae.wuyunxing.webdav.library.play.util;

import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Utils {
	private static final String TAG = Utils.class.getSimpleName();

	/**
	 * The extension separator character.
	 */
	public static final char EXTENSION_SEPARATOR = '.';

	/**
	 * The Unix separator character.
	 */
	private static final char UNIX_SEPARATOR = '/';

	/**
	 * The Windows separator character.
	 */
	private static final char WINDOWS_SEPARATOR = '\\';

	/**
	 * Check the port is being used or not.
	 *
	 * @param port the port to be checked.
	 * @return true if the port is used, false otherwise.
	 */
	public static boolean checkPort(String addr, int port) {
		try {
			InetAddress theAddress = InetAddress.getByName(addr);
			try {
				Socket theSocket = new Socket(theAddress, port);
				theSocket.close();
				return false;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
		} catch (UnknownHostException e) {
			Log.e(TAG, e.getMessage());
		}
		return true;
	}

	public static ContentType getContentType(String uri) {
		if (uri == null) {
			return ContentType.create("*/*");
		}

		String ext = Utils.getFileExtension(uri);
		Log.d(TAG, "ext = " + ext);

		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		Log.d(TAG, "mime type = " + mimeType);
		return ContentType.create(mimeType);
	}

	/**
	 * Gets the extension of a filename.
	 * <p>
	 * This method returns the textual part of the filename after the last dot.
	 * There must be no directory separator after the dot.
	 * <pre>
	 * foo.txt      --> "txt"
	 * a/b/c.jpg    --> "jpg"
	 * a/b.txt/c    --> ""
	 * a/b/c        --> ""
	 * </pre>
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename the filename to retrieve the extension of.
	 * @return the extension of the file or an empty string if none exists or {@code null}
	 * if the filename is {@code null}.
	 */
	public static String getFileExtension(String filename) {
		if (filename == null) {
			return null;
		}
		int index = indexOfExtension(filename);
		if (index == -1) {
			return "";
		} else {
			return filename.substring(index + 1);
		}
	}

	/**
	 * Returns the index of the last extension separator character, which is a dot.
	 * <p>
	 * This method also checks that there is no directory separator after the last dot.
	 * To do this it uses {@link #indexOfLastSeparator(String)} which will
	 * handle a file in either Unix or Windows format.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename  the filename to find the last path separator in, null returns -1
	 * @return the index of the last separator character, or -1 if there
	 * is no such character
	 */
	public static int indexOfExtension(String filename) {
		if (filename == null) {
			return -1;
		}
		int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
		int lastSeparator = indexOfLastSeparator(filename);
		return lastSeparator > extensionPos ? -1 : extensionPos;
	}

	/**
	 * Returns the index of the last directory separator character.
	 * <p>
	 * This method will handle a file in either Unix or Windows format.
	 * The position of the last forward or backslash is returned.
	 * <p>
	 * The output will be the same irrespective of the machine that the code is running on.
	 *
	 * @param filename  the filename to find the last path separator in, null returns -1
	 * @return the index of the last separator character, or -1 if there
	 * is no such character
	 */
	public static int indexOfLastSeparator(String filename) {
		if (filename == null) {
			return -1;
		}
		int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
		int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
		return Math.max(lastUnixPos, lastWindowsPos);
	}
}
