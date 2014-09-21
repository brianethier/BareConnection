package com.barenode.bareconnection;



public class RestProperties {

    private volatile String mUrl;
    private volatile String mUsername;
    private volatile String mPassword;
    private volatile int mConnectTimeout = RestConnection.DEFAULT_CONNECT_TIMEOUT;
    private volatile int mReadTimeout = RestConnection.DEFAULT_SOCKET_TIMEOUT;
    
    
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

