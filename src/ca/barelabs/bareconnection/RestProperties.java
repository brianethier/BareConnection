package ca.barelabs.bareconnection;



public class RestProperties {

    private final String mUrl;
    private final String mPath;
    private final String mUsername;
    private final String mPassword;
    private final int mConnectTimeout;
    private final int mReadTimeout;
    
    public RestProperties(String url, String path, String username, String password, int connectTimeout, int readTimeout) {
        mUrl = url;
        mPath = path;
        mUsername = username;
        mPassword = password;
        mConnectTimeout = connectTimeout;
        mReadTimeout = readTimeout;
    }

    public String getUrl() {
        return mUrl;
    }
    
    public String getPath() {
        return mPath;
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
    
    
    public static class Builder {
        
        private String mUrl;
        private String mPath;
        private String mUsername;
        private String mPassword;
        private int mConnectTimeout = RestConnection.DEFAULT_CONNECT_TIMEOUT;
        private int mReadTimeout = RestConnection.DEFAULT_SOCKET_TIMEOUT;

        
        public Builder url(String url) {
            mUrl = url;
            return this;    
        }
        
        public Builder path(String path) {
            mPath = path;
            return this;    
        }
        
        public Builder username(String username) {
            mUsername = username;
            return this;    
        }
        
        public Builder password(String password) {
            mPassword = password;
            return this;    
        }
        
        public Builder connectTimeout(int connectTimeout) {
            mConnectTimeout = connectTimeout;
            return this;    
        }
        
        public Builder readTimeout(int readTimeout) {
            mReadTimeout = readTimeout;
            return this;    
        }
        
        public RestProperties build() {
            return new RestProperties(mUrl, mPath, mUsername, mPassword, mConnectTimeout, mReadTimeout);
        }
    }
}

