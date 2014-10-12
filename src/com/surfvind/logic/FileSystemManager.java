package com.surfvind.logic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileSystemManager {

	/**
	 * Mount a directory in the filesystem
	 * 
	 * @param dir
	 * @return creation status
	 */
	public static boolean createDir(String dir) {
		File f = new File(dir);
		if (f.exists()) {
			return true;
		}

		return f.mkdir();
	}

	/**
	 * Load a file
	 * 
	 * @param fileName
	 * @return the loaded file or null if the file could not be found
	 */
	public static File load(String fileName) {
		File f = new File(fileName);
		if (f.exists()) {
			return f;
		}
		return null;
	}

	/**
	 * Stores the content as a file (with name @fileName) under location
	 * 
	 * @param content
	 * @param location
	 * @param fileName
	 * @return true if file was saved successfully, false otherwise
	 */
	public static boolean save(String content, String location, String fileName) {
		File f;
		FileWriter fw;
		BufferedWriter bw;

		f = new File(location + fileName);

		try {
			fw = new FileWriter(f);
			bw = new BufferedWriter(fw);
			bw.append(content);
			bw.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Returns all files in dir
	 * @param dir the folder to look in
	 * @return
	 */
	public static File[] listFilesIn(File dir) {
		if (dir == null) {
			return new File[0];
		}

		if (!dir.isDirectory()) {
			return new File[0];
		}

		return dir.listFiles();
	}

	/**
	 * Returns all files in a folder represented as strings
	 * @param dir to look in as file
	 * @returns an array of files. The array is empty in case of no files found
	 */
	public static String[] listFilesInAsString(File dir) {
		String[] filesAsString;
		File[] files;
		files = listFilesIn(dir);

		filesAsString = new String[files.length];
		for (int i = 0; i < files.length; i++) {
			filesAsString[i] = files[i].getName();
		}

		return filesAsString;
	}

	/**
	 * Returns all files in a folder represented as strings
	 * @param dir to look in
	 * @returns an array of files. The array is empty in case of no files found
	 */
	public static String[] listFilesInAsString(String dir) {
		return listFilesInAsString(load(dir));
	}

}
