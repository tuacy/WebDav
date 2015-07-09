package com.vae.wuyunxing.webdav.library;

import android.content.Context;

import com.vae.wuyunxing.webdav.library.exception.ConstructorException;
import com.vae.wuyunxing.webdav.library.exception.IllegalDirectoryPathException;
import com.vae.wuyunxing.webdav.library.exception.PathNotFoundException;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitFileExplorer;
import com.vae.wuyunxing.webdav.library.imp.jackrabbit.JackrabbitPath;
import com.vae.wuyunxing.webdav.library.imp.local.LocalFileExplorer;
import com.vae.wuyunxing.webdav.library.imp.local.LocalPath;

public class FileBrowserFactory {

	public static FileExplorer createLocalFileExplorer(LocalPath rootPath) throws PathNotFoundException, IllegalDirectoryPathException {
		return new LocalFileExplorer(rootPath);
	}

	public static FileExplorer createJackrabbitFileExplorer(JackrabbitPath jackrabbitPath, Context context)
		throws PathNotFoundException, IllegalDirectoryPathException, ConstructorException {
		return new JackrabbitFileExplorer(jackrabbitPath, context);
	}
}
