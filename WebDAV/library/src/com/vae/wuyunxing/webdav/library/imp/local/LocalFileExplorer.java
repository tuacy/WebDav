package com.vae.wuyunxing.webdav.library.imp.local;

import com.vae.wuyunxing.webdav.library.DotDotFile;
import com.vae.wuyunxing.webdav.library.DotFile;
import com.vae.wuyunxing.webdav.library.FileExplorer;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.exception.AccessViolationException;
import com.vae.wuyunxing.webdav.library.exception.DirectoryAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.FileAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.IllegalDirectoryPathException;
import com.vae.wuyunxing.webdav.library.exception.NoSuchFileException;
import com.vae.wuyunxing.webdav.library.exception.PathNotFoundException;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.util.PathUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class LocalFileExplorer implements FileExplorer {

	private File mRootDir;
	private File mCurrentDir;

	private enum PathType {
		CURRENT,
		PARENT,
		HOME,
		ROOT_BASE,
		PARENT_BASE,
		CURRENT_BASE,
		INVALID
	}

	/**
	 * Constructor function
	 *
	 * @param rootPath: the root path
	 * @throws IllegalDirectoryPathException exception (directory)
	 */
	public LocalFileExplorer(LocalPath rootPath) throws IllegalDirectoryPathException {
		if (rootPath == null) {
			throw new NullPointerException("Illegal parameter, rootPath cannot be null!");
		}

		File file = new File(rootPath.getFullPath());
		if (file.isFile()) {
			throw new IllegalDirectoryPathException(" is not directory!");
		}
		mRootDir = file;
		mCurrentDir = file;
	}

	/**
	 * Check whether current path if valid
	 *
	 * @param current: the check path string
	 * @return true: valid, false: not
	 */
	private boolean isCurrentPathValid(String current) {
		return current.startsWith(mRootDir.getPath());
	}

	/**
	 * Diagnose the path type
	 *
	 * @param path: the path string
	 * @return the path type
	 */
	private static PathType diagnosePathType(String path) {
		path = path.trim();

		if (path.equals(".")) {
			return PathType.CURRENT; /** . represent current path */
		} else if (path.equals("..")) {
			return PathType.PARENT; /** .. represent parent path */
		} else if (path.equals("~")) {
			return PathType.HOME;   /** ~ represent home path */
		} else if (path.matches("^/\\S*")) {                            /** e.g. /home/mktech/mk_smart_router */
			return PathType.ROOT_BASE;
		} else if (path.matches("^(../)+(\\S+(/\\S+)*)*")) {            /** e.g. ../../mktech/mk_smart_router */
			return PathType.PARENT_BASE;
		} else if (path.matches("^\\S+(/\\S+)*")) {                        /** e.g. mktech/mk_smart_router */
			return PathType.CURRENT_BASE;
		} else {
			return PathType.INVALID;
		}
	}

	/**
	 * Change the dir to root
	 */
	private void changeDirToRoot() {
		updatePwd(mRootDir);
	}

	/**
	 * Change the dir to parent
	 */
	private void changeDirToParent() {
		if (isRoot()) {
			return;
		}

		String parent = mCurrentDir.getParent();
		File file = new File(parent);
		updatePwd(file);
	}

	/**
	 * Change the dir to absolute
	 * @param path: absolute path
	 * @throws IllegalDirectoryPathException
	 * @throws PathNotFoundException
	 */
	private void changeDirToAbsoluteUrl(String path) throws IllegalDirectoryPathException, PathNotFoundException {
		File file = new File(path);
		if (file.isFile()) {
			throw new IllegalDirectoryPathException(path + " is not directory!");
		}

		updatePwd(file);
	}

	/**
	 * Update the pwd(current work dir)
	 * @param file: destination dir
	 */
	private void updatePwd(File file) {
		mCurrentDir = file;
	}

	/**
	 * Retriever the path
	 * @param path: base path
	 * @return destination path
	 */
	private String retrieveUrlByBase(String path) {
		String root = mRootDir.getPath();
		String current = mCurrentDir.getPath();
		return PathUtil.retrieve(path.startsWith("/") ? root : current, path);
	}

	/**
	 * List the file
	 * @param withDot: with .
	 * @param withHiddenFile: with hidden file
	 * @return file list
	 * @throws AccessViolationException
	 */
	private List<FileInfo> listFiles(boolean withDot, boolean withHiddenFile) throws AccessViolationException {
		File currentDir = mCurrentDir;
		File[] files;
		if (withHiddenFile) {
			files = currentDir.listFiles();
		} else {
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					return !file.isHidden();
				}
			};
			files = currentDir.listFiles(filter);
		}
		List<FileInfo> fileList;
		if (withDot) {
			fileList = new ArrayList<FileInfo>(files.length + 2);
			fileList.add(new DotFile());
			fileList.add(new DotDotFile());
		} else {
			fileList = new ArrayList<FileInfo>(files.length);
		}
		for (File f : files) {
			LocalFileInfo info = LocalFileInfo.create(f);
			fileList.add(info);
		}
		return fileList;
	}

	/**
	 * New file with absolute path
	 * @param path: file absolute path
	 * @return true: success, false: failure
	 * @throws FileAlreadyExistsException
	 */
	private boolean makeFileWithAbsoluteUrl(String path) throws FileAlreadyExistsException {
		try {
			File file = new File(path);
			if (file.exists()) {
				throw new FileAlreadyExistsException(path + " is already exists!");
			}
			return file.createNewFile();
		} catch (IOException e) {
			MKLog.e(LocalFileExplorer.class, "%s is not a file.", path);
		}
		return false;
	}

	/**
	 * New directory with absolute path
	 * @param path: directory absolute path
	 * @return true: success, false: failure
	 * @throws DirectoryAlreadyExistsException
	 */
	private boolean makeDirWithAbsoluteUrl(String path) throws DirectoryAlreadyExistsException {
		File dir = new File(path);
		if (dir.exists()) {
			throw new DirectoryAlreadyExistsException(path + " is already exists!");
		}
		return dir.mkdirs();
	}

	/**
	 * Delete directory or file
	 * @param path: directory or file path
	 * @return true: success, false: failure
	 */
	private boolean removeDirOrFile(String path) throws NoSuchFileException {
		File file = new File(path);
		if (!file.exists()) {
			throw new NoSuchFileException(path + " is not exists!");
		}
		return file.delete();
	}


	/**
	 * Move with absolute path
	 * @param srcPath: source path
	 * @param destPath: destination path
	 * @param delete: if source exists whether delete source
	 * @return true: success, false: failure
	 * @throws IllegalDirectoryPathException
	 * @throws PathNotFoundException
	 * @throws NoSuchFileException
	 */
	private boolean moveWithAbsoluteUrl(String srcPath, String destPath, boolean delete)
		throws IllegalDirectoryPathException, PathNotFoundException, NoSuchFileException {
		File srcFile = new File(srcPath);
		if (!srcFile.exists()) {
			throw new NoSuchFileException("Cannot found source " + srcPath);
		}

		File destFile = new File(destPath);

		if (srcFile.renameTo(destFile)) {
			if (delete) {
				return srcFile.delete();
			}
		}

		return false;
	}

	/**
	 * Move without absolute path
	 * @param src: source path
	 * @param dest: destination path
	 * @param delete: whether delete source file
	 * @return true: success, false: failure
	 * @throws AccessViolationException
	 * @throws PathNotFoundException
	 * @throws IllegalDirectoryPathException
	 * @throws NoSuchFileException
	 */
	private boolean moveSrcToDest(String src, String dest, boolean delete)
		throws AccessViolationException, PathNotFoundException, IllegalDirectoryPathException, NoSuchFileException {
		String srcUrl, destUrl;
		/** get source absolute path */
		switch (diagnosePathType(src)) {
			case CURRENT:
			case PARENT:
				return false;
			case HOME:
				throw new AccessViolationException("Illegal src, you cannot copy the root to anywhere!");
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				srcUrl = retrieveUrlByBase(src);
				if (!isCurrentPathValid(srcUrl)) {
					throw new AccessViolationException("Illegal src, you cannot copy " + srcUrl + " to anywhere!");
				}
				break;
			case INVALID:
			default:
				return false;
		}
		/** get destination absolute path */
		switch (diagnosePathType(dest)) {
			case CURRENT:
				destUrl = mCurrentDir.getPath();
				break;
			case PARENT:
				destUrl = mCurrentDir.getParent();
				if (!isCurrentPathValid(destUrl)) {
					throw new AccessViolationException("Illegal dest, you cannot copy things to " + destUrl);
				}
				break;
			case HOME:
				destUrl = mRootDir.getPath();
				break;
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				destUrl = retrieveUrlByBase(dest);
				if (!isCurrentPathValid(destUrl)) {
					throw new AccessViolationException("Illegal dest, you cannot copy things to " + destUrl);
				}
				break;
			case INVALID:
			default:
				return false;
		}

		return moveWithAbsoluteUrl(srcUrl, destUrl, delete);
	}

	@Override
	public boolean isRoot() {
		return mRootDir.getPath().equals(mCurrentDir.getPath());
	}

	@Override
	public FileInfo getRootPath() {
		return LocalFileInfo.create(mRootDir);
	}

	@Override
	public void cd(String dir) throws IllegalDirectoryPathException, PathNotFoundException, AccessViolationException {
		if (dir == null || dir.length() == 0) {
			return;
		}
		switch (diagnosePathType(dir)) {
			case CURRENT:
				break;
			case PARENT:
				changeDirToParent();
				break;
			case HOME:
				changeDirToRoot();
				break;
			case ROOT_BASE:    /** e.g. /home/mktech/mk_smart_router */
			case PARENT_BASE:  /** e.g. ../../mktech/mk_smart_router */
			case CURRENT_BASE: /** e.g. mktech/mk_smart_router */
				/** get absolute path */
				String url = retrieveUrlByBase(dir);
				if (isCurrentPathValid(url)) {
					changeDirToAbsoluteUrl(url);
				} else {
					throw new AccessViolationException("Illegal path, you cannot access " + url);
				}
				break;
			case INVALID:
			default:
				throw new IllegalDirectoryPathException(dir + " is illegal path!");
		}
	}

	@Override
	public String getCurrentDir() {
		return mCurrentDir.getName();
	}

	@Override
	public String pwd(Boolean withoutBaseUrl) {
		String rootPath = mRootDir.getPath();
		String curPath = mCurrentDir.getPath();
		return curPath.replaceAll(rootPath.endsWith("/") ? rootPath.substring(0, rootPath.length() - 1) : rootPath, "");
	}

	@Override
	public List<FileInfo> ls(String... parameters) throws InvalidParameterException, AccessViolationException {
		if (parameters == null || parameters.length > 1) {
			throw new InvalidParameterException("Parameter is invalid!");
		}

		if (parameters.length == 0) {
			return listFiles(false, false);
		} else if (parameters[0].equals("-l")) {
			return listFiles(false, true);
		} else if (parameters[0].equals("-a")) {
			return listFiles(true, false);
		} else if (parameters[0].equals("-al")) {
			return listFiles(true, true);
		} else {
			throw new InvalidParameterException("Parameter is invalid!");
		}
	}

	@Override
	public boolean mkdir(String dir) throws DirectoryAlreadyExistsException, AccessViolationException {
		if (dir == null || dir.length() == 0) {
			return false;
		}
		/** get absolute path */
		switch (diagnosePathType(dir)) {
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				String url = retrieveUrlByBase(dir);
				if (isCurrentPathValid(url)) {
					return makeDirWithAbsoluteUrl(url);
				} else {
					throw new AccessViolationException("Illegal dir, you cannot make directory in " + url);
				}
			default:
				break;
		}
		return false;
	}

	@Override
	public boolean touch(String name) throws FileAlreadyExistsException, AccessViolationException {
		if (name == null || name.length() == 0) {
			return false;
		}
		/** get absolute path */
		switch (diagnosePathType(name)) {
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				String url = retrieveUrlByBase(name);
				if (isCurrentPathValid(url)) {
					return makeFileWithAbsoluteUrl(url);
				} else {
					throw new AccessViolationException("Illegal name, you cannot make file " + url);
				}
			default:
				break;
		}
		return false;
	}

	@Override
	public boolean rm(String path) throws AccessViolationException, NoSuchFileException {
		if (path == null || path.length() == 0) {
			return false;
		}
		/** get absolute path */
		switch (diagnosePathType(path)) {
			case CURRENT:
			case PARENT:
			case HOME:
				throw new AccessViolationException("Illegal path, you cannot remove directory " + path);
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				String url = retrieveUrlByBase(path);
				if (isCurrentPathValid(url)) {
					return removeDirOrFile(url);
				} else {
					throw new AccessViolationException("Illegal path, you cannot remove directory " + url);
				}
			case INVALID:
			default:
				break;
		}
		return false;
	}

	@Override
	public boolean cp(String src, String dest)
		throws AccessViolationException, IllegalDirectoryPathException, PathNotFoundException, NoSuchFileException,
			   DirectoryAlreadyExistsException {
		return !(src == null || src.length() == 0) && !(dest == null || dest.length() == 0) && moveSrcToDest(src, dest, false);
	}

	@Override
	public boolean mv(String src, String dest)
		throws IllegalDirectoryPathException, AccessViolationException, PathNotFoundException, NoSuchFileException,
			   DirectoryAlreadyExistsException {
		return !(src == null || src.length() == 0) && !(dest == null || dest.length() == 0) && moveSrcToDest(src, dest, true);
	}

	@Override
	public boolean renameFileOrDir(String old, String newName)
		throws AccessViolationException, PathNotFoundException, IllegalDirectoryPathException, NoSuchFileException {
		return false;
	}
}
