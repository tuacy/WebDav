package com.vae.wuyunxing.webdav.library.util;

import java.text.DecimalFormat;

public class FileSizeConvertUtils {

	private final static int SIZE_B = 1024;
	private final static int SIZE_K = 1048576;
	private final static int SIZE_M = 1073741824;

	public static String formatFileSizeUnit(long filebyte) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (filebyte < SIZE_B) {
			fileSizeString = df.format((double) filebyte) + "B";
		} else if (filebyte < SIZE_K) {
			fileSizeString = df.format((double) filebyte / SIZE_B) + "K";
		} else if (filebyte < SIZE_M) {
			fileSizeString = df.format((double) filebyte / SIZE_K) + "M";
		} else {
			fileSizeString = df.format((double) filebyte / SIZE_M) + "G";
		}
		return fileSizeString;
	}
}
