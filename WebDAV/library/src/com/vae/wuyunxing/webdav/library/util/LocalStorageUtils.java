package com.vae.wuyunxing.webdav.library.util;

import android.os.Environment;
import android.os.StatFs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocalStorageUtils {

	public static final int KEY_STORAGE_NAME = 11;
	public static final int KEY_STORAGE_PATH = 12;

	public static boolean externalMemoryAvailable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static String getExternalPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		if (sdDir == null) {
			return null;
		}
		return sdDir.toString();
	}

	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	public static long getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return -1;
		}
	}

	public static long getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else {
			return -1;
		}
	}

	public static List<Map<Integer, String>> getCurStorageList() {
		List<Map<Integer, String>> list = new ArrayList<Map<Integer, String>>();
		String extraPath = LocalStorageUtils.getExternalPath();
		if (extraPath != null) {
			Map<Integer, String> temp = new HashMap<Integer, String>();
			String name = new String(extraPath);
			name = name.substring(name.lastIndexOf(File.separator));
			temp.put(KEY_STORAGE_NAME, name);
			temp.put(KEY_STORAGE_PATH, extraPath);
			list.add(temp);
		}

		String tfPath = LocalStorageUtils.getTFCardPath();
		if (tfPath != null) {
			Map<Integer, String> temp = new HashMap<Integer, String>();
			String name = new String(tfPath);
			name = name.substring(name.lastIndexOf(File.separator));
			temp.put(KEY_STORAGE_NAME, name);
			temp.put(KEY_STORAGE_PATH, tfPath);
			list.add(temp);
		}
		return list;
	}

	public static String getTFCardPath() {
		try {
			FileReader fr = new FileReader("/system/etc/vold.fstab");
			BufferedReader br = new BufferedReader(fr);
			while (br.ready()) {
				String line = br.readLine().trim();
				if (line.startsWith("dev_mount")) {
					String[] arrStr = line.split(" ");
					if (arrStr.length >= 5 && arrStr[0].equals("dev_mount") && arrStr[1].equals("sdcard")) {
						br.close();
						File file = new File(arrStr[2]);
						if (file.exists()) {
							File f = new File(arrStr[2]);
							if (f.canWrite()) {
								return arrStr[2];
							}
						}
					}
				}
			}
		} catch (Exception e) {
			String path = "";
			Map<String, String> map = System.getenv();
			if (map.containsKey("SECONDARY_STORAGE")) {
				path = map.get("SECONDARY_STORAGE").split(":")[0];
			} else if (map.containsKey("EXTERNAL_STORAGE")) {
				path = map.get("EXTERNAL_STORAGE");
			}
			File file = new File(path);
			if (file.exists()) {
				File f = new File(path);
				if (f.canWrite()) {
					return path;
				}
			}
		}
		String sdPath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(sdPath);
		if (file.exists()) {
			return sdPath;
		} else {
			File SDfiles = Environment.getExternalStorageDirectory();
			if (SDfiles != null) {
				File parentFile = SDfiles.getParentFile();
				File[] listFiles = parentFile.listFiles();
				for (int i = 0; i < listFiles.length; i++) {
					File file2 = new File(listFiles[i].getPath());
					if (listFiles[i].canWrite() && file2.exists()) {
						return listFiles[i].getPath();
					}
				}
			}
		}
		return null;
	}
}
