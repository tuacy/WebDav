package com.vae.wuyunxing.webdav.library.imp.jackrabbit;

import android.content.Context;

import com.vae.wuyunxing.webdav.library.FileExplorer;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.exception.AccessViolationException;
import com.vae.wuyunxing.webdav.library.exception.DirectoryAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.FileAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.IllegalDirectoryPathException;
import com.vae.wuyunxing.webdav.library.exception.NoSuchFileException;
import com.vae.wuyunxing.webdav.library.exception.PathNotFoundException;

import java.security.InvalidParameterException;
import java.util.List;

public class JackrabbitFileExplorer implements FileExplorer {

	public JackrabbitFileExplorer(JackrabbitPath rootPath, Context context) throws IllegalDirectoryPathException {

	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public FileInfo getRootPath() {
		return null;
	}

	@Override
	public void cd(String dir) throws IllegalDirectoryPathException, PathNotFoundException, AccessViolationException {

	}

	@Override
	public String getCurrentDir() {
		return null;
	}

	@Override
	public String pwd(Boolean withoutBaseUrl) {
		return null;
	}

	@Override
	public List<FileInfo> ls(String... parameters) throws InvalidParameterException, AccessViolationException {
		return null;
	}

	@Override
	public boolean mkdir(String dir) throws DirectoryAlreadyExistsException, AccessViolationException {
		return false;
	}

	@Override
	public boolean touch(String name) throws FileAlreadyExistsException, AccessViolationException {
		return false;
	}

	@Override
	public boolean rm(String path) throws AccessViolationException, NoSuchFileException {
		return false;
	}

	@Override
	public boolean cp(String src, String dest)
		throws AccessViolationException, IllegalDirectoryPathException, PathNotFoundException, NoSuchFileException,
			   DirectoryAlreadyExistsException {
		return false;
	}

	@Override
	public boolean mv(String src, String dest)
		throws IllegalDirectoryPathException, AccessViolationException, PathNotFoundException, NoSuchFileException,
			   DirectoryAlreadyExistsException {
		return false;
	}

	@Override
	public boolean renameFileOrDir(String old, String newName)
		throws AccessViolationException, PathNotFoundException, IllegalDirectoryPathException, NoSuchFileException {
		return false;
	}
}
