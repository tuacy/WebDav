package com.vae.wuyunxing.webdav.library.play.webdav;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WebDAVFileEntity extends AbstractHttpEntity {

	/** WebDAV file length */
	private final long         mContentLength;
	/** WebDAV file stream */
	private final InputStream mContentStream;

	/**
	 * constructor function
	 * @param stream: WebDAV file InputStream
	 * @param contentLength: WebDAV file length
	 * @param contentType: WebDAV file type
	 */
	public WebDAVFileEntity(InputStream stream, long contentLength, ContentType contentType) {
		mContentLength = contentLength;
		mContentStream = stream;
		setContentType(contentType.toString());
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public long getContentLength() {
		return mContentLength;
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return mContentStream;
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		Args.notNull(outputStream, "Output stream");
		final int outputBufferSize = 8 * 1024;
		final InputStream in = mContentStream;
		try {
			final byte[] tmp = new byte[outputBufferSize];
			int l;
			while ((l = in.read(tmp)) != -1) {
				outputStream.write(tmp, 0, l);
			}
			outputStream.flush();
		} finally {
			in.close();
		}
	}

	@Override
	public boolean isStreaming() {
		return false;
	}
}
