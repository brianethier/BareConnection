package com.barenode.bareconnection;



public class RestProperties {

	private String mUrl;
	private String mUsername;
	private String mPassword;
	private int mConnectTimeout;
	private int mReadTimeout;
	
	
	public String getUrl() {
		return mUrl;
	}
	
	public String getUsername() {
		return mUsername;
	}
	
	public String getPassword() {
		return mPassword;
	}
	
	public int getConnectTimeout() {
		return mConnectTimeout;
	}
	
	public int getReadTimeout() {
		return mReadTimeout;
	}
	
	public RestProperties url(String url) {
		mUrl = url;
		return this;	
	}
	
	public RestProperties username(String username) {
		mUsername = username;
		return this;	
	}
	
	public RestProperties password(String password) {
		mPassword = password;
		return this;	
	}
	
	public RestProperties connectTimeout(int connectTimeout) {
		mConnectTimeout = connectTimeout;
		return this;	
	}
	
	public RestProperties readTimeout(int readTimeout) {
		mReadTimeout = readTimeout;
		return this;	
	}
}

