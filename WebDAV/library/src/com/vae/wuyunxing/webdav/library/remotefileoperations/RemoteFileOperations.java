package com.vae.wuyunxing.webdav.library.remotefileoperations;

import java.io.InputStream;
import java.util.List;

public interface RemoteFileOperations<A, B> {

	public int BUF_SIZE = 64 * 1024;

	/**
	 * put file to router
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean put(A a, B b);

	/**
	 * write file
	 * @param a
	 * @param info
	 * @return
	 */
	public boolean write(A a, String info);

	/**
	 * get file from fouter
	 * @param a
	 * @param b
	 * @return
	 */
	public boolean get(A a, B b);

	/**
	 * delete file (alarm message config file)
	 * @param list
	 */
	public void delete(List<A> list);

	/**
	 * get inputStream
	 * @param a
	 * @return
	 */
	public InputStream getStream(A a);
}
