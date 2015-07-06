package com.vae.wuyunxing.webdav.library;


import com.vae.wuyunxing.webdav.library.exception.AccessViolationException;
import com.vae.wuyunxing.webdav.library.exception.DirectoryAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.FileAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.IllegalDirectoryPathException;
import com.vae.wuyunxing.webdav.library.exception.NoSuchFileException;
import com.vae.wuyunxing.webdav.library.exception.PathNotFoundException;

import java.security.InvalidParameterException;
import java.util.List;

public interface FileExplorer {

	/**
	 * Diagnose current directory is whether the root directory or not.
	 *
	 * @return Return {@code true} if current directory is root directory, {@code false} otherwise.
	 */
	public boolean isRoot();

	/**
	 * Get the root path.
	 *
	 * @return Return the root path.
	 */
	public FileInfo getRootPath();

	/**
	 * Change the directory to the given path. The given path can be current path("."),
	 * parent path(".."), relative path("../a/b/", "a/b/c") or absolute path("/a/b/c/").
	 * The path should be end with a slash("/").
	 *
	 * @param dir The path to change to.
	 * @throws IllegalDirectoryPathException
	 * @throws PathNotFoundException
	 * @throws AccessViolationException
	 */
	public void cd(String dir) throws IllegalDirectoryPathException, PathNotFoundException, AccessViolationException;

	/**
	 * Return current directory.
	 *
	 * @return Current directory.
	 */
	public String getCurrentDir();

	/**
	 * Return the absolute path of working directory.
	 *
	 * @return Absolute path of working directory.
	 */
	public String pwd(Boolean withoutBaseUrl);

	/**
	 * Return all files in current directory.
	 *
	 * @param parameters If {@code parameters} is empty, only return all not hidden files
	 *                   without current path(".") and parent path("..").
	 *                   If {@code parameters} is "-l", return all files including hidden files
	 *                   without current path(".") and parent path("..").
	 *                   If {@code parameters} is "-a", return all not hidden files with
	 *                   current path(".") and parent path("..").
	 *                   If {@code parameters} is "-al", return all files including hidden files
	 *                   with current path(".") and parent path("..").
	 * @return All files in current directory.
	 * @throws InvalidParameterException
	 * @throws AccessViolationException
	 */
	public List<FileInfo> ls(String... parameters) throws InvalidParameterException, AccessViolationException;

	/**
	 * Make a new directory. The given directory path can be relative path("../a/b/", "a/b/c") or absolute path("/a/b/c").
	 *
	 * @param dir The new directory name or path.
	 * @return Return {@code true} if success, {@code false} if failed.
	 * @throws DirectoryAlreadyExistsException
	 * @throws AccessViolationException
	 */
	public boolean mkdir(String dir) throws DirectoryAlreadyExistsException, AccessViolationException;

	/**
	 * Make a new file. The given file path can be relative path("../a/b/test.txt", "a/b/test.txt") or absolute path("/a/b/abc.txt").
	 *
	 * @param name The new file name or path.
	 * @return Return {@code true} if success, {@code false} if failed.
	 * @throws FileAlreadyExistsException
	 * @throws AccessViolationException
	 */
	public boolean touch(String name) throws FileAlreadyExistsException, AccessViolationException;

	/**
	 * Delete a directory or file.
	 *
	 * @param path The directory path or file path.
	 *             The given path can be relative path("../a/b/", "a/b/c") or absolute path("/a/b/c").
	 * @return Return {@code true} if success, {@code false} if failed.
	 * @throws AccessViolationException
	 * @throws NoSuchFileException
	 */
	public boolean rm(String path) throws AccessViolationException, NoSuchFileException;

	/**
	 * Copy a directory or file.
	 *
	 * @param src Current directory or file path.
	 *            The given path can be relative path("../a/b/", "a/b/c") or absolute path("/a/b/c").
	 * @param dest Destination the directory or file copy to.
	 *             The given path can be relative path("../a/b/", "a/b/c") or absolute path("/a/b/c").
	 * @return Return {@code true} if success, {@code false} if failed.
	 * @throws AccessViolationException
	 * @throws IllegalDirectoryPathException
	 * @throws PathNotFoundException
	 * @throws NoSuchFileException
	 */
	public boolean cp(String src, String dest)
		throws AccessViolationException, IllegalDirectoryPathException, PathNotFoundException, NoSuchFileException,
			   DirectoryAlreadyExistsException;

	/**
	 * Move a directory or file.
	 *
	 * @param src Current directory or file path.
	 *            The given path can be relative path("../a/b/", "a/b/c") or absolute path("/a/b/c").
	 * @param dest Destination the directory or file copy to.
	 *             The given path can be relative path("../a/b/", "a/b/c") or absolute path("/a/b/c").
	 * @return Return {@code true} if success, {@code false} if failed.
	 * @throws IllegalDirectoryPathException
	 * @throws AccessViolationException
	 * @throws PathNotFoundException
	 * @throws NoSuchFileException
	 */
	public boolean mv(String src, String dest)
		throws IllegalDirectoryPathException, AccessViolationException, PathNotFoundException, NoSuchFileException,
			   DirectoryAlreadyExistsException;

	public boolean renameFileOrDir(String old, String newName)
		throws AccessViolationException, PathNotFoundException, IllegalDirectoryPathException, NoSuchFileException;
}
