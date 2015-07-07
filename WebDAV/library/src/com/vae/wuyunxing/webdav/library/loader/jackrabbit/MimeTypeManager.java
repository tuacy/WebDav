package com.vae.wuyunxing.webdav.library.loader.jackrabbit;

public class MimeTypeManager implements MimeMapTable {

	public static String getMimeTypeString(String filePath) {

		String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
		suffix = suffix.toLowerCase();
		for (int index = 0; index < MIME_MAP_TABLE.length; index++) {
			String temp[] = MIME_MAP_TABLE[index];
			if (temp[0].equals(suffix)) {
				return temp[1];
			}
 		}
		return null;
	}
}
