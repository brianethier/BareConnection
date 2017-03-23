package ca.barelabs.bareconnection;

import java.io.FilterInputStream;
import java.io.InputStream;

public class ContentInputStream extends FilterInputStream {
	
	private final String mContentType;
	private final int mContentLength;
	
	public ContentInputStream(InputStream in, String contentType) {
		this(in, contentType, -1);
	}

	public ContentInputStream(InputStream in, String contentType, int contentLength) {
		super(in);
		mContentType = contentType;
		mContentLength = contentLength;
	}

	public String getContentType() {
		return mContentType;
	}

	public int getContentLength() {
		return mContentLength;
	}
}
