package com.vae.wuyunxing.webdav.library.util;

import com.vae.wuyunxing.webdav.library.FileCategory;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.filter.FileFilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FileUtil {

	public static String fileTimeToString(Calendar calendar, String pattern) {
		SimpleDateFormat formater = new SimpleDateFormat(pattern);
		return formater.format(calendar.getTime());
	}

	public static String getSuffix(String file) {
		int index = file.lastIndexOf(".");
		if (index > -1) {
			return file.substring(index + 1);
		}
		return "";
	}

	public static FileCategory filterFileCategory(String file) {
		if (file == null) {
			throw new NullPointerException("Parameter cannot be null!");
		}

		String filename = file.toLowerCase();
		for (FileCategory category : FileCategory.values()) {
			for (String suffix : category.getSuffixes()) {
				if (filename.endsWith('.' + suffix.toLowerCase())) {
					return category;
				}
			}
		}
		return FileCategory.OTHERS;
	}

	public static List<FileInfo> filter(List<FileInfo> list, FileFilter filter) {
		List<FileInfo> newList = new ArrayList<FileInfo>();
		for (FileInfo file : list) {
			if (filter.accept(file)) {
				newList.add(file);
			}
		}
		return newList;
	}

}
