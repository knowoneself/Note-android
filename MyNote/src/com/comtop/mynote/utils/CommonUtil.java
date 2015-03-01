package com.comtop.mynote.utils;

import java.io.File;

import android.os.Environment;

public class CommonUtil {

	/**
	 * isFileExists
	 * @param filePath
	 * @return
	 */
	public static boolean isFileExists(String filePath) {
		try {
			File file = new File(filePath);
			return file.exists();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	/**
	 * deleteSdCardFile
	 * @param filePath
	 * @return
	 */
	public static boolean deleteSdCardFile(String filePath) {
		try {
			File file = new File(filePath);
			return file.delete();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	
	/**
	 * createFileDir
	 * @param path
	 */
	public static void createFileDir(String path) {
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	
	/**
	 * getSdCardPath
	 * @return
	 */
	public static String getSdCardPath() {
		String path;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			path = Environment.getExternalStorageDirectory().getPath();
		} else {
			path = Environment.getDataDirectory().getAbsolutePath();
		}
		return path;
	}

}
