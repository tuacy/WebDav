package com.vae.wuyunxing.webdav.library.imp.jackrabbit;

import android.content.Context;
import android.net.Uri;

import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.WebdavEntry;
import com.owncloud.android.lib.common.network.WebdavUtils;
import com.owncloud.android.lib.resources.files.FileUtils;
import com.owncloud.android.lib.resources.files.RemoteFile;
import com.vae.wuyunxing.webdav.library.DotDotFile;
import com.vae.wuyunxing.webdav.library.DotFile;
import com.vae.wuyunxing.webdav.library.FileExplorer;
import com.vae.wuyunxing.webdav.library.FileInfo;
import com.vae.wuyunxing.webdav.library.exception.AccessViolationException;
import com.vae.wuyunxing.webdav.library.exception.ConstructorException;
import com.vae.wuyunxing.webdav.library.exception.DirectoryAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.FileAlreadyExistsException;
import com.vae.wuyunxing.webdav.library.exception.IllegalDirectoryPathException;
import com.vae.wuyunxing.webdav.library.exception.NoSuchFileException;
import com.vae.wuyunxing.webdav.library.exception.PathNotFoundException;
import com.vae.wuyunxing.webdav.library.log.MKLog;
import com.vae.wuyunxing.webdav.library.util.PathUtil;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpStatus;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.client.methods.DavMethodBase;
import org.apache.jackrabbit.webdav.client.methods.DeleteMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;


public class JackrabbitFileExplorer implements FileExplorer {

	private static final int GET_FILE_SYNC_READ_TIMEOUT       = 40000;
	private static final int GET_FILE_SYNC_CONNECTION_TIMEOUT = 5000;
	private static final int MKDIR_READ_TIMEOUT               = 30000;
	private static final int MKDIR_CONNECTION_TIMEOUT         = 5000;
	private static final int REMOVE_READ_TIMEOUT              = 30000;
	private static final int REMOVE_CONNECTION_TIMEOUT        = 5000;
	private static final int MOVE_READ_TIMEOUT                = 600000;
	private static final int MOVE_CONNECTION_TIMEOUT          = 5000;
	private static final int EXISTENCE_CHECK_TIMEOUT          = 10000;
	private static final int RENAME_READ_TIMEOUT              = 600000;
	private static final int RENAME_CONNECTION_TIMEOUT        = 5000;

	private final OwnCloudClient mClient;
	private final RemoteFile     mRootDir;
	private final Credentials    mCredentials;
	private       RemoteFile     mCurrentDir;

	private enum PathType {
		CURRENT,
		PARENT,
		HOME,
		HTTP,
		ROOT_BASE,
		PARENT_BASE,
		CURRENT_BASE,
		INVALID
	}

	/**
	 * Construct function
	 * @param rootPath: JackrabbitPaht
	 * @param context: Context
	 * @throws IllegalDirectoryPathException
	 * @throws ConstructorException
	 */
	public JackrabbitFileExplorer(JackrabbitPath rootPath, Context context) throws IllegalDirectoryPathException, ConstructorException {
		if (rootPath == null) {
			throw new NullPointerException("Illegal parameter, rootPath cannot be null!");
		}

		String url = rootPath.getUrl();
		MKLog.d(JackrabbitFileExplorer.class, "JackrabbitFileExplorer: %s", url);
		/** client */
		mClient = OwnCloudClientFactory.createOwnCloudClient(Uri.parse(rootPath.getBaseUrl()), context, true);
		mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(rootPath.getUser(), rootPath.getPassword()));
		mCredentials = rootPath.getCredentials();

		/** if the file exits will return false other will return true so we no need check the return value */
		executeMakeDir(mClient, url, true);

		/** get the remote file info */
		RemoteFile remoteFile = executeGetFile(mClient, url, true);

		/** check is directory */
		if (null != remoteFile) {
			if (!remoteFile.isDirectory()) {
				throw new IllegalDirectoryPathException(url + " is not directory!");
			}
			mRootDir = remoteFile;
			mCurrentDir = remoteFile;
			MKLog.d(JackrabbitFileExplorer.class, "mRootDir.getUri        : %s", mRootDir.getUri());
			MKLog.d(JackrabbitFileExplorer.class, "mRootDir.getRemotePath : %s", mRootDir.getRemotePath());
			MKLog.d(JackrabbitFileExplorer.class, "mRootDir.getParent     : %s", mRootDir.getParent());
		} else {
			mRootDir = null;
			mCurrentDir = null;
			MKLog.d(JackrabbitFileExplorer.class, "JackrabbitFileExplorer Constructor function get remote file error");
			throw new ConstructorException(url + " constructor error!");
		}
	}

//	/**
//	 * Check is file exits
//	 * @param client: client
//	 * @param path: paht
//	 * @param absolutePath: is absolute path
//	 * @return
//	 */
//	public static boolean executeExistenceCheck(OwnCloudClient client, String path, boolean absolutePath) {
//
//		HeadMethod head = null;
//		boolean existence = false;
//		String url = absolutePath ? Uri.encode(path, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(path);
//		MKLog.d(JackrabbitFileExplorer.class, "executeExistenceCheck: %s", url);
//
//		try {
//			head = new HeadMethod(url);
//			int status = client.executeMethod(head, EXISTENCE_CHECK_TIMEOUT, EXISTENCE_CHECK_TIMEOUT);
//			client.exhaustResponse(head.getResponseBodyAsStream());
//			existence = (status == HttpStatus.SC_OK);
//			MKLog.d(JackrabbitFileExplorer.class, "executeExistenceCheck: %s, %d, %b", url, status, existence);
//		} catch (HttpException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			if (head != null) {
//				head.releaseConnection();
//			}
//		}
//		MKLog.d(JackrabbitFileExplorer.class, "executeExistenceCheck: %s, %b", url, existence);
//		return existence;
//	}

	/**
	 * Get remote file
	 * @param client: client
	 * @param path: path
	 * @param absolutePath: true(eg: path = http://192.168.1.6:8080/Screenshots/), false(eg: path = /Screenshots/)
	 * @return
	 */
	public static RemoteFile executeGetFile(OwnCloudClient client, String path, boolean absolutePath) {
		PropFindMethod propFind = null;
		/** get absolute path */
		/** fix bug some WebDAV server mCurrentDir.getUri() return with out http://192........ **/
		String url = null;
		if (path.startsWith(client.getWebdavUri().toString())) {
			url = Uri.encode(path, ":/");
		} else {
			url = client.getWebdavUri() + WebdavUtils.encodePath(path);
		}
//		String url = absolutePath ? Uri.encode(path, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(path);
		MKLog.d(JackrabbitFileExplorer.class, "executeGetFile: %s", url);
		try {
			int status;
			propFind = new PropFindMethod(url, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_0);
			status = client.executeMethod(propFind, GET_FILE_SYNC_READ_TIMEOUT, GET_FILE_SYNC_CONNECTION_TIMEOUT);

			boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);
			if (isSuccess) {
				MultiStatus resp = propFind.getResponseBodyAsMultiStatus();
				WebdavEntry we = new WebdavEntry(resp.getResponses()[0], client.getWebdavUri().getPath());
				MKLog.d(JackrabbitFileExplorer.class, "executeGetFile success");
				/** get remote file */
				return new RemoteFile(we);
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (DavException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (propFind != null) {
				propFind.releaseConnection();
			}
		}
		MKLog.d(JackrabbitFileExplorer.class, "executeGetFile failed");
		return null;
	}

	/**
	 * Conversion MultiStatus to RemoteFile list
	 * @param remoteData: MultiStatus
	 * @param client: OwnCloudClient
	 * @return: RemoteFile list
	 */
	private static List<RemoteFile> readData(MultiStatus remoteData, OwnCloudClient client) {

		List<RemoteFile> fileList = new ArrayList<RemoteFile>();
		for (int i = 1; i < remoteData.getResponses().length; ++i) {
			WebdavEntry we = new WebdavEntry(remoteData.getResponses()[i], client.getWebdavUri().getPath());
			/** conversion WebdavEntry to RemoteFile */
			RemoteFile remoteFile = new RemoteFile(we);
			MKLog.d(JackrabbitFileExplorer.class, "listfile: %s, length: %d", remoteFile.getUri(), remoteFile.getLength());
			fileList.add(remoteFile);
		}
		return fileList;
	}

	/***
	 * Get remote file list
	 * @param client: client
	 * @param path: path
	 * @param absolutePath: is absolute path
	 * @return
	 */
	private static List<RemoteFile> executeListFiles(OwnCloudClient client, String path, boolean absolutePath) {
		PropFindMethod query = null;
		List<RemoteFile> fileList = null;

		/** get absolute path */
		String url = null;
		if (path.startsWith(client.getWebdavUri().toString())) {
			url = Uri.encode(path, ":/");
		} else {
			url = client.getWebdavUri() + WebdavUtils.encodePath(path);
		}
//		String url = absolutePath ? Uri.encode(path, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(path);
		MKLog.d(JackrabbitFileExplorer.class, "executeListFiles: %s", url);
		try {
			query = new PropFindMethod(url, DavConstants.PROPFIND_ALL_PROP, DavConstants.DEPTH_1);
			int status = client.executeMethod(query);

			boolean isSuccess = (status == HttpStatus.SC_MULTI_STATUS || status == HttpStatus.SC_OK);
			if (isSuccess) {
				MultiStatus dataInServer = query.getResponseBodyAsMultiStatus();
				fileList = readData(dataInServer, client);
				MKLog.d(JackrabbitFileExplorer.class, "executeListFiles success");
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (DavException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (query != null) {
				query.releaseConnection();
			}
		}
		MKLog.d(JackrabbitFileExplorer.class, "executeListFiles failed");
		return fileList;
	}

	@Override
	public boolean isRoot() {
		MKLog.d(JackrabbitFileExplorer.class, "isRoot; %s, %s", mRootDir.getRemotePath(), mCurrentDir.getRemotePath());
		return mRootDir.getRemotePath().equals(mCurrentDir.getRemotePath());
	}

	@Override
	public FileInfo getRootPath() {
		return JackrabbitFileInfo.create(mRootDir);
	}

	/**
	 * Diagnose the path type
	 *
	 * @param path: the path string
	 * @return the path type
	 */
	private static PathType diagnosePathType(String path) {
		path = path.trim();

		/** check path type */
		if (path.equals(".")) {
			return PathType.CURRENT;
		} else if (path.equals("..")) {
			return PathType.PARENT;
		} else if (path.equals("~")) {
			return PathType.HOME;
		} else if (path.matches("^http://\\d*(.\\d*){3}.*")) { //e.g. http://192.168.11.1
			return PathType.HTTP;
		} else if (path.matches("^/.*")) {                            //e.g. /home/mktech/mk_smart_router
			return PathType.ROOT_BASE;
		} else if (path.matches("^(../)+(.+(/.+)*)*")) {            //e.g. ../../mktech/mk_smart_router
			return PathType.PARENT_BASE;
		} else if (path.matches("^.+(/.+)*")) {                        //e.g. mktech/mk_smart_router
			return PathType.CURRENT_BASE;
		} else {
			return PathType.INVALID;
		}
	}

	private void updatePwd(RemoteFile dir) {
		mCurrentDir = dir;
	}

	/**
	 * Change the dir to parent
	 */
	private void changeDirToParent() throws AccessViolationException {
		if (isRoot()) {
			return;
		}

		String parent = mCurrentDir.getParent();
		MKLog.d(JackrabbitFileExplorer.class, "parent: %s", parent);
		RemoteFile file;
		try {
			file = executeGetFile(mClient, parent, false);
			updatePwd(file);
		} catch (Exception e) {
			throw new AccessViolationException("Strange! Current path's parent : " + parent + ", is not directory!", e);
		}
	}

	/**
	 * Change the dir to root
	 */
	private void changeDirToRoot() {
		updatePwd(mRootDir);
	}

	/**
	 * Check whether current path is valid
	 *
	 * @param current: the check path string
	 * @return true: valid, false: not
	 */
	private boolean isCurrentPathValid(String current) {
		return current.startsWith(mRootDir.getUri());
	}

	/**
	 * Change the dir to absolute
	 * @param path: absolute path
	 * @throws IllegalDirectoryPathException
	 * @throws PathNotFoundException
	 */
	private void changeDirToAbsoluteUrl(String path) throws IllegalDirectoryPathException, PathNotFoundException {
		RemoteFile remoteFile;
		try {
			remoteFile = executeGetFile(mClient, path, true);
		} catch (Exception e) {
			throw new PathNotFoundException("Cannot found directory " + path);
		}
		if (null != remoteFile) {
			if (!remoteFile.isDirectory()) {
				throw new IllegalDirectoryPathException(path + " is not directory!");
			}
		}
		updatePwd(remoteFile);
	}

	/**
	 * Retriever the path
	 * @param path: base path
	 * @return destination path
	 */
	private String retrieveUrlByBase(String path) {
		String root = mRootDir.getUri();
		String current = mCurrentDir.getUri();
		MKLog.d(JackrabbitFileExplorer.class, "retrieveUrlByBase: %s, %s", path,
				PathUtil.retrieve(path.startsWith("/") ? root : current, path));
		return PathUtil.retrieve(path.startsWith("/") ? root : current, path);
	}

	@Override
	public void cd(String path) throws IllegalDirectoryPathException, PathNotFoundException, AccessViolationException {
		if (path == null || path.length() == 0) {
			return;
		}
		switch (diagnosePathType(path)) {
			case CURRENT:
				break;
			case PARENT:
				changeDirToParent();
				break;
			case HOME:
				changeDirToRoot();
				break;
			case HTTP:
				if (isCurrentPathValid(path)) {
					changeDirToAbsoluteUrl(path);
				} else {
					throw new AccessViolationException("Illegal path, you cannot access " + path);
				}
				break;
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				String url = retrieveUrlByBase(path);
				if (isCurrentPathValid(url)) {
					changeDirToAbsoluteUrl(url);
				} else {
					throw new AccessViolationException("Illegal path, you cannot access " + url);
				}
				break;
			case INVALID:
			default:
				throw new IllegalDirectoryPathException(path + " is illegal path!");
		}
	}

	@Override
	public String getCurrentDir() {
		return mCurrentDir.getName();
	}

	@Override
	public String pwd(Boolean withoutBaseUrl) {
		String rootPath = mRootDir.getUri();
        /** fix issue some WebDAV mRootDir.getUri() with out http://192.168. */
        if (!rootPath.startsWith(mClient.getBaseUri().toString())) {
            rootPath = mClient.getBaseUri() + rootPath;
        }
		String curPath = mCurrentDir.getUri();
        if (!curPath.startsWith(mClient.getBaseUri().toString())) {
            curPath = mClient.getBaseUri() + curPath;
        }

		if (withoutBaseUrl) {
			return curPath.replaceAll(rootPath.endsWith("/") ? rootPath.substring(0, rootPath.length() - 1) : rootPath, "");
		} else {
			return curPath;
		}
	}

	private List<FileInfo> listFiles(boolean withDot, boolean withHiddenFile) throws AccessViolationException {
		RemoteFile currentDir = mCurrentDir;
		List<RemoteFile> files;
		if (withHiddenFile) {
			files = executeListFiles(mClient, currentDir.getRemotePath(), false);
		} else {
			files = executeListFiles(mClient, currentDir.getRemotePath(), false);
		}

		List<FileInfo> fileList;
		if (withDot) {
			fileList = new ArrayList<FileInfo>(files.size() + 2);
			fileList.add(new DotFile());
			fileList.add(new DotDotFile());
		} else {
			fileList = new ArrayList<FileInfo>(files.size());
		}
		for (RemoteFile f : files) {
			JackrabbitFileInfo info = JackrabbitFileInfo.create(f);
			fileList.add(info);
		}
		return fileList;
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

	private static String getParentUri(String url) {
		char separatorChar = '/';
		StringBuilder builder = new StringBuilder(url);
		int length = builder.length();
		if (builder.charAt(length - 1) == separatorChar) {
			builder.deleteCharAt(length - 1);
		}

		int index = builder.lastIndexOf(FileUtils.PATH_SEPARATOR);
		if (index == -1) {
			return null;
		}
		return builder.substring(0, index);
	}

	/**
	 * mkdir (if the file exits will return false, other will mkdir it and return true)
	 * @param client: OwnCloudClient
	 * @param path: String
	 * @param absolutePath: boolean(absolute url eg: paht = http://192.168.1.6:8080/Screenshots/ not absolute url eg: path = /Screenshots/)
	 * @return
	 */
	public static boolean executeMakeDir(OwnCloudClient client, String path, boolean absolutePath) {
		MkColMethod mkCol = null;
		String url = null;
		if (path.startsWith(client.getWebdavUri().toString())) {
			url = Uri.encode(path, ":/");
		} else {
			url = client.getWebdavUri() + WebdavUtils.encodePath(path);
		}
//		String url = absolutePath ? Uri.encode(path, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(path);
		MKLog.d(JackrabbitFileExplorer.class, "executeMakeDir: %s", url);
		try {
			mkCol = new MkColMethod(url);
			int status = client.executeMethod(mkCol, MKDIR_READ_TIMEOUT, MKDIR_CONNECTION_TIMEOUT);
			if (!mkCol.succeeded() && status == HttpStatus.SC_CONFLICT) {
				if (executeMakeDir(client, getParentUri(path), absolutePath)) {
					mkCol = new MkColMethod(url);
					client.executeMethod(mkCol, MKDIR_READ_TIMEOUT, MKDIR_CONNECTION_TIMEOUT);
				}
			}
			MKLog.d(JackrabbitFileExplorer.class, "executeMakeDir %b", mkCol.succeeded());
			return mkCol.succeeded();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mkCol != null) {
				mkCol.releaseConnection();
			}
		}
		MKLog.d(JackrabbitFileExplorer.class, "executeMakeDir failed");
		return false;
	}

	/**
	 * make dir with absolute path
	 * @param url
	 * @return
	 * @throws DirectoryAlreadyExistsException
	 */
	private boolean makeDirWithAbsoluteUrl(String url) throws DirectoryAlreadyExistsException {
		RemoteFile dir = executeGetFile(mClient, url, false);
		if (dir != null) {
			throw new DirectoryAlreadyExistsException(url + " is already exists!");
		}

		return executeMakeDir(mClient, url, false);
	}

	@Override
	public boolean mkdir(String dir) throws DirectoryAlreadyExistsException, AccessViolationException {
		if (dir == null || dir.length() == 0) {
			return false;
		}
		String url = dir;
		switch (diagnosePathType(dir)) {
			case HTTP:
				url = dir;
				break;
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				url = retrieveUrlByBase(dir);
			default:
				break;
		}

		if (isCurrentPathValid(url)) {
			return makeDirWithAbsoluteUrl(url);
		} else {
			throw new AccessViolationException("Illegal dir, you cannot make directory in " + dir);
		}
	}

	@Override
	public boolean touch(String name) throws FileAlreadyExistsException, AccessViolationException {
		return false;
	}

	/**
	 * Remove directory of file
	 * @param client
	 * @param path
	 * @param absolutePath
	 * @return
	 */
	private static boolean executeRemoveDirOrFile(OwnCloudClient client, String path, boolean absolutePath) {
		DeleteMethod delete = null;

		/** get absolute path */
		String url = null;
		if (path.startsWith(client.getWebdavUri().toString())) {
			url = Uri.encode(path, ":/");
		} else {
			url = client.getWebdavUri() + WebdavUtils.encodePath(path);
		}
//		String url = absolutePath ? Uri.encode(path, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(path);
		MKLog.d(JackrabbitFileExplorer.class, "executeRemoveDirOrFile: %s", url);
		try {
			delete = new DeleteMethod(url);
			int status = client.executeMethod(delete, REMOVE_READ_TIMEOUT, REMOVE_CONNECTION_TIMEOUT);
			MKLog.d(JackrabbitFileExplorer.class, "executeRemoveDirOrFile: %b", (delete.succeeded() || status == HttpStatus.SC_NOT_FOUND));
			return (delete.succeeded() || status == HttpStatus.SC_NOT_FOUND);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (delete != null) {
				delete.releaseConnection();
			}
		}

		MKLog.d(JackrabbitFileExplorer.class, "executeRemoveDirOrFile failed");
		return false;
	}

	@Override
	public boolean rm(String path) throws AccessViolationException {
		if (path == null || path.length() == 0) {
			return false;
		}
		switch (diagnosePathType(path)) {
			case CURRENT:
			case PARENT:
			case HOME:
				throw new AccessViolationException("Illegal path, you cannot remove directory " + path);
			case HTTP:
				if (isCurrentPathValid(PathUtil.retrieve(path, ".."))) {
					return executeRemoveDirOrFile(mClient, path, true);
				} else {
					throw new AccessViolationException("Illegal path, you cannot remove directory " + path);
				}
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				String url = retrieveUrlByBase(path);
				if (isCurrentPathValid(url)) {
					return executeRemoveDirOrFile(mClient, url, false);
				} else {
					throw new AccessViolationException("Illegal path, you cannot remove directory " + url);
				}
			case INVALID:
			default:
				break;
		}
		return false;
	}

	/**
	 * Move file
	 * @param client
	 * @param src
	 * @param dest
	 * @param absolutePath
	 * @return
	 */
	public static boolean executeMoveFile(OwnCloudClient client, String src, String dest, boolean absolutePath) {

		MoveMethod move = null;
		String srcUrl = null;
		if (src.startsWith(client.getWebdavUri().toString())) {
			srcUrl = Uri.encode(src, ":/");
		} else {
			srcUrl = client.getWebdavUri() + WebdavUtils.encodePath(src);
		}
//		String srcUrl = absolutePath ? Uri.encode(src, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(src);
		String destUrl = null;
		if (dest.startsWith(client.getWebdavUri().toString())) {
			destUrl = Uri.encode(dest, ":/");
		} else {
			destUrl = client.getWebdavUri() + WebdavUtils.encodePath(dest);
		}
//		String destUrl = absolutePath ? Uri.encode(dest, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(dest);
		MKLog.d(JackrabbitFileExplorer.class, "executeMoveFile src: %s", srcUrl);
		MKLog.d(JackrabbitFileExplorer.class, "executeMoveFile dst: %s", destUrl);
		try {
			move = new MoveMethod(srcUrl, destUrl, true);
			int status = client.executeMethod(move, MOVE_READ_TIMEOUT, MOVE_CONNECTION_TIMEOUT);
			MKLog.d(JackrabbitFileExplorer.class, "executeMoveFile, status: %d", status);
			client.exhaustResponse(move.getResponseBodyAsStream());
			if (status == HttpStatus.SC_CREATED || status == HttpStatus.SC_NO_CONTENT) {
				return true;
			} else {
				return false;
			}

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (move != null) {
				move.releaseConnection();
			}
		}
		MKLog.d(JackrabbitFileExplorer.class, "executeMoveFile failed");
		return false;
	}

	/**
	 * Move with absolute path
	 * @param srcUrl
	 * @param destUrl
	 * @return
	 * @throws IllegalDirectoryPathException
	 * @throws PathNotFoundException
	 * @throws NoSuchFileException
	 * @throws DirectoryAlreadyExistsException
	 */
	private boolean moveWithAbsoluteUrl(String srcUrl, String destUrl)
		throws IllegalDirectoryPathException, PathNotFoundException, NoSuchFileException, DirectoryAlreadyExistsException {
		RemoteFile srcFile, destFile;
		srcFile = executeGetFile(mClient, srcUrl, true);
		if (srcFile == null) {
			throw new NoSuchFileException("Cannot found source " + srcUrl);
		}

		destFile = executeGetFile(mClient, destUrl, true);
		if (destFile != null) {
			throw new DirectoryAlreadyExistsException(destUrl + " is already exists!");
		}

		executeMoveFile(mClient, srcUrl, destUrl, true);
		return true;
	}

	/**
	 * Move src to dest
	 * @param src
	 * @param dest
	 * @param delete
	 * @return
	 * @throws AccessViolationException
	 * @throws PathNotFoundException
	 * @throws IllegalDirectoryPathException
	 * @throws NoSuchFileException
	 * @throws DirectoryAlreadyExistsException
	 */
	private boolean moveSrcToDest(String src, String dest, boolean delete)
		throws AccessViolationException, PathNotFoundException, IllegalDirectoryPathException, NoSuchFileException,
			   DirectoryAlreadyExistsException {
		String srcUrl, destUrl;
		/** get src absolute path */
		switch (diagnosePathType(src)) {
			case CURRENT:
			case PARENT:
				return false;
			case HOME:
				throw new AccessViolationException("Illegal src, you cannot copy the root to anywhere!");
			case HTTP:
				if (isCurrentPathValid(PathUtil.retrieve(src, ".."))) {
					srcUrl = src;
				} else {
					throw new AccessViolationException("Illegal src, you cannot copy " + src + " to anywhere!");
				}
				break;
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
		/** get dest absolute path */
		switch (diagnosePathType(dest)) {
			case CURRENT:
				destUrl = mCurrentDir.getRemotePath();
				break;
			case PARENT:
				destUrl = mCurrentDir.getParent();
				if (!isCurrentPathValid(destUrl)) {
					throw new AccessViolationException("Illegal dest, you cannot copy things to " + destUrl);
				}
				break;
			case HOME:
				destUrl = mRootDir.getRemotePath();
				break;
			case HTTP:
				if (isCurrentPathValid(PathUtil.retrieve(dest, ".."))) {
					destUrl = dest;
				} else {
					throw new AccessViolationException("Illegal dest, you cannot copy things to " + dest);
				}
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

		/** add filename to destUrl */
		if (!destUrl.endsWith("/")) {
			destUrl = destUrl + "/";
		}
		String lastComponent = PathUtil.getLastComponent(srcUrl);
		destUrl = destUrl.concat(lastComponent);

		boolean result = moveWithAbsoluteUrl(srcUrl, destUrl);
		if (result && delete) {
			executeRemoveDirOrFile(mClient, srcUrl, true);
		}
		return result;
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

	/**
	 * Rename
	 * @param client
	 * @param oldName
	 * @param newName
	 * @param absolutePath
	 * @return
	 */
	public static boolean executeRename(OwnCloudClient client, String oldName, String newName, boolean absolutePath) {
		boolean result = false;
		String oldUrl = null;
		if (oldName.startsWith(client.getWebdavUri().toString())) {
			oldUrl = Uri.encode(oldName, ":/");
		} else {
			oldUrl = client.getWebdavUri() + WebdavUtils.encodePath(oldName);
		}
//		String oldUrl = absolutePath ? Uri.encode(oldName, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(oldName);
		String newUrl = null;
		if (newName.startsWith(client.getWebdavUri().toString())) {
			newUrl = Uri.encode(newName, ":/");
		} else {
			newUrl = client.getWebdavUri() + WebdavUtils.encodePath(newName);
		}
//		String newUrl = absolutePath ? Uri.encode(newName, ":/") : client.getWebdavUri() + WebdavUtils.encodePath(newName);
		LocalMoveMethod move = null;
		MKLog.d(JackrabbitFileExplorer.class, "executeRename ole: %s", oldUrl);
		MKLog.d(JackrabbitFileExplorer.class, "executeRename new: %s", newUrl);
		try {
			move = new LocalMoveMethod(oldUrl, newUrl);
			int status = client.executeMethod(move, RENAME_READ_TIMEOUT, RENAME_CONNECTION_TIMEOUT);
			MKLog.d(JackrabbitFileExplorer.class, "executeRename status: %d", status);
			result = move.succeeded();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (move != null) {
				move.releaseConnection();
			}
		}

		MKLog.d(JackrabbitFileExplorer.class, "executeRename: %b", result);
		return result;
	}

	private static class LocalMoveMethod extends DavMethodBase {

		public LocalMoveMethod(String uri, String dest) {
			super(uri);
			addRequestHeader(new Header("Destination", dest));
		}

		@Override
		public String getName() {
			return "MOVE";
		}

		@Override
		protected boolean isSuccess(int status) {
			return status == 201 || status == 204;
		}

	}

	@Override
	public boolean renameFileOrDir(String old, String newName)
		throws AccessViolationException, PathNotFoundException, IllegalDirectoryPathException, NoSuchFileException {
		String srcUrl, destUrl;
		switch (diagnosePathType(old)) {
			case CURRENT:
			case PARENT:
			case HOME:
				return false;
			case HTTP:
				if (isCurrentPathValid(PathUtil.retrieve(old, ".."))) {
					srcUrl = old;
				} else {
					throw new AccessViolationException("Illegal src, you cannot copy " + old + " to anywhere!");
				}
				break;
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				srcUrl = retrieveUrlByBase(old);
				if (!isCurrentPathValid(srcUrl)) {
					throw new AccessViolationException("Illegal src, you cannot copy " + srcUrl + " to anywhere!");
				}
				break;
			case INVALID:
			default:
				return false;
		}
		switch (diagnosePathType(newName)) {
			case CURRENT:
			case PARENT:
			case HOME:
				return false;
			case HTTP:
				if (isCurrentPathValid(PathUtil.retrieve(newName, ".."))) {
					destUrl = newName;
				} else {
					throw new AccessViolationException("Illegal dest, you cannot copy things to " + newName);
				}
				break;
			case ROOT_BASE:
			case PARENT_BASE:
			case CURRENT_BASE:
				destUrl = retrieveUrlByBase(newName);
				if (!isCurrentPathValid(destUrl)) {
					throw new AccessViolationException("Illegal dest, you cannot copy things to " + destUrl);
				}
				break;
			case INVALID:
			default:
				return false;
		}

		MKLog.d(JackrabbitFileExplorer.class, "srcUrl:" + srcUrl);
		MKLog.d(JackrabbitFileExplorer.class, "destUrl:" + destUrl);

		return executeRename(mClient, srcUrl, destUrl, true);
	}
}
