/*
 * Copyright 2014 Brian Ethier
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ca.barelabs.bareconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RestConnection {
    
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_TRACE = "TRACE";
    public static final String METHOD_CONNECT = "CONNECT";

    public static final int SC_UNKNOWN = -1;
	
    public static final int SC_OK = 200;
    public static final int SC_CREATED = 201;
    public static final int SC_ACCEPTED = 202;
    public static final int SC_NOT_AUTHORITATIVE = 203;
    public static final int SC_NO_CONTENT = 204;
    public static final int SC_RESET = 205;
    public static final int SC_PARTIAL = 206;
	
    public static final int SC_MULT_CHOICE = 300;
    public static final int SC_MOVED_PERM = 301;
    public static final int SC_MOVED_TEMP = 302;
    public static final int SC_SEE_OTHER = 303;
    public static final int SC_NOT_MODIFIED = 304;
    public static final int SC_USE_PROXY = 305;

    public static final int SC_BAD_REQUEST = 400;
    public static final int SC_UNAUTHORIZED = 401;
    public static final int SC_PAYMENT_REQUIRED = 402;
    public static final int SC_FORBIDDEN = 403;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_BAD_METHOD = 405;
    public static final int SC_NOT_ACCEPTABLE = 406;
    public static final int SC_PROXY_AUTH = 407;
    public static final int SC_CLIENT_TIMEOUT = 408;
    public static final int SC_CONFLICT = 409;
    public static final int SC_GONE = 410;
    public static final int SC_LENGTH_REQUIRED = 411;
    public static final int SC_PRECON_FAILED = 412;
    public static final int SC_ENTITY_TOO_LARGE = 413;
    public static final int SC_REQ_TOO_LONG = 414;
    public static final int SC_UNSUPPORTED_TYPE = 415;

    public static final int SC_INTERNAL_ERROR = 500;
    public static final int SC_NOT_IMPLEMENTED = 501;
    public static final int SC_BAD_GATEWAY = 502;
    public static final int SC_UNAVAILABLE = 503;
    public static final int SC_GATEWAY_TIMEOUT = 504;
    public static final int SC_VERSION = 505;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
    public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    public static final String KEY_CHARSET = "charset";
    public static final String KEY_BOUNDARY = "boundary";

    public static final String AUTHORIZATION_TYPE_BASIC = "Basic";
    
    public static final String ENCODING_GZIP = "gzip";
    
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";

    public static final String PATH_SEPARATOR = "/";
    public static final String QUERY_SEPARATOR = "?";
    
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    public static final int DEFAULT_MAX_RETRY_ATTEMPTS = 5;
    
    public interface MultipartFormWriter {
        void onWrite(OutputStream out, String charset, String boundary) throws IOException;
    }


    private final HttpURLConnectionFactory mFactory;
    private ObjectParser mParser = ObjectParser.getDefault();
    private int mMaxRetryAttempts = DEFAULT_MAX_RETRY_ATTEMPTS;
    private boolean mRetryOnIOException;
    private BackOffPolicy mBackOffPolicy;
    private String mContentType = CONTENT_TYPE_JSON;
    private String mIncomingCharset = DEFAULT_CHARSET;
    private String mOutgoingCharset = DEFAULT_CHARSET;


    public RestConnection(HttpURLConnectionFactory factory) {
        if (factory == null) {
            throw new IllegalStateException("RestConnection must be created with an HttpURLConnectionFactory. Also see RestConnection.Builder");
        }
        mFactory = factory;
    }
    
    public ObjectParser getParser() {
        return mParser;
    }

    public void setParser(ObjectParser parser) {
        mParser = parser;
    }

    public int getMaxRetryAttempts() {
        return mMaxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        mMaxRetryAttempts = maxRetryAttempts;
    }
    
    public boolean isRetryOnIOException() {
        return mRetryOnIOException;
    }
    
    public void setRetryOnIOException(boolean retryOnIOException) {
        mRetryOnIOException = retryOnIOException;
    }
    
    public BackOffPolicy getBackOffPolicy() {
        return mBackOffPolicy;
    }
    
    public void setBackOffPolicy(BackOffPolicy backOffPolicy) {
        mBackOffPolicy = backOffPolicy;
    }
    
    public String getContentType() {
        return mContentType;
    }
    
    public void setContentType(String contentType) {
        mContentType = contentType;
    }
    
    public String getIncomingCharset() {
        return mIncomingCharset;
    }
    
    public void setIncomingCharset(String incomingCharset) {
        mIncomingCharset = incomingCharset;
    }

    public String getOutgoingCharset() {
        return mOutgoingCharset;
    }
    
    public void setOutgoingCharset(String outgoingCharset) {
        mOutgoingCharset = outgoingCharset;
    }

    public RestResponse head() throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_HEAD, null);
    }

    public RestResponse get() throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_GET, null);
    }

    public RestResponse put() throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_PUT, null);
    }

    public RestResponse put(Object object) throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_PUT, object);
    }

    public RestResponse post() throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_POST, null);
    }

    public RestResponse post(List<Entity> entities) throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_POST, new DefaultMultipartFormWriter(entities));
    }

    public RestResponse post(Object object) throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_POST, object);
    }

    public RestResponse delete() throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(METHOD_DELETE, null);
    }
    
    public RestResponse execute(String method) throws MalformedURLException, UnsupportedEncodingException, IOException {
        return execute(method, null);
    }
    
    public RestResponse execute(String method, Object object) throws MalformedURLException, UnsupportedEncodingException, IOException {
        boolean validResponse = false;
        if (mBackOffPolicy != null) {
            mBackOffPolicy.reset();
        }
        int attempts = 0;
        while (true) {
            boolean retryAllowed = attempts++ < mMaxRetryAttempts;
            HttpURLConnection connection = mFactory.createHttpURLConnection(method);
            try {
                if (object != null) {
                    String boundary = Long.toHexString(System.currentTimeMillis());
                    connection.setRequestProperty(HEADER_CONTENT_TYPE, encodeContentType(object, boundary)); 
                    connection.setDoOutput(true);
                    write(connection.getOutputStream(), object, boundary);
                }
                RestResponse response = new RestResponse(connection, mParser, mIncomingCharset);
                if (retryAllowed && mBackOffPolicy != null && mBackOffPolicy.isBackOffRequired(response.getStatusCode())) {
                    // If this returns false then we went over the max back off time, so don't don't try again
                    if (mBackOffPolicy.backOff()) {
                        continue;
                    }
                }
                validResponse = true;
                return response;
            } catch (IOException e) {
                if (retryAllowed && mRetryOnIOException) {
                    continue;
                }
                throw e;
            } finally {
                if (!validResponse) {
                    connection.disconnect();
                }
            }
        } 
    }
    
    private String encodeContentType(Object object, String boundary) {
        if (object instanceof Map) {
            return CONTENT_TYPE_FORM_URLENCODED + ";" + KEY_CHARSET + "=" + mOutgoingCharset;
        } else if (object instanceof MultipartFormWriter) {
            return CONTENT_TYPE_MULTIPART_FORM + ";" + KEY_BOUNDARY + "=" + boundary;
        } else {
            return mContentType + ";" + KEY_CHARSET + "=" + mOutgoingCharset;
        }
    }

    private void write(OutputStream out, Object object, String boundary) throws IOException {
        if (object instanceof InputStream) {
            IOUtils.copy((InputStream) object, out);
            out.flush();
            out.close();
        } else if (object instanceof String) {
            String data = (String) object;
            out.write(data.getBytes(mOutgoingCharset));
            out.flush();
            out.close();
        } else if (object instanceof Map) {
            String query = RestUtils.toQuery((Map<?,?>) object, mOutgoingCharset);
            out.write(query.getBytes(mOutgoingCharset));
            out.flush();
            out.close();
        } else if (object instanceof MultipartFormWriter) {
            MultipartFormWriter writer = (MultipartFormWriter) object;
            writer.onWrite(out, mOutgoingCharset, boundary);
            out.flush();
            out.close();
        } else {
            mParser.saveAndClose(object, out, mOutgoingCharset);
        }
    }
    
    
    public static final class Builder {

        public interface OnPrepareConnectionListener {
            void onPrepareConnection(HttpURLConnection connection);
        }

        private OnPrepareConnectionListener mListener;   
        private ObjectParser mParser = ObjectParser.getDefault();
        private int mMaxRetryAttempts = DEFAULT_MAX_RETRY_ATTEMPTS;
        private boolean mRetryOnIOException;
        private BackOffPolicy mBackOffPolicy;
        private RestProperties.Builder mPropertiesBuilder = new RestProperties.Builder();
        private String mAuthorizationType = AUTHORIZATION_TYPE_BASIC;
        private String mContentType = CONTENT_TYPE_JSON;
        private String mIncomingCharset = DEFAULT_CHARSET;
        private String mOutgoingCharset = DEFAULT_CHARSET;
        private HashMap<String, Object> mParams = new HashMap<>();
        private List<String> mCookies;
        
        public Builder listener(OnPrepareConnectionListener listener) {
            mListener = listener;
            return this;    
        }
        
        public Builder parser(ObjectParser parser) {
            mParser = parser;
            return this;    
        }
        
        public Builder maxRetryAttempts(int maxRetryAttempts) {
            mMaxRetryAttempts = maxRetryAttempts;
            return this;    
        }
        
        public Builder retryOnIOException(boolean retryOnIOException) {
            mRetryOnIOException = retryOnIOException;
            return this;    
        }
        
        public Builder backOffPolicy(BackOffPolicy backOffPolicy) {
            mBackOffPolicy = backOffPolicy;
            return this;    
        }
        
        public Builder url(String url) {
            mPropertiesBuilder.url(url);
            return this;    
        }
        
        public Builder path(String path) {
            mPropertiesBuilder.path(path);
            return this;    
        }
        
        public Builder username(String username) {
            mPropertiesBuilder.username(username);
            return this;    
        }
        
        public Builder password(String password) {
            mPropertiesBuilder.password(password);
            return this;    
        }
        
        public Builder connectTimeout(int connectTimeout) {
            mPropertiesBuilder.connectTimeout(connectTimeout);
            return this;    
        }
        
        public Builder readTimeout(int readTimeout) {
            mPropertiesBuilder.readTimeout(readTimeout);
            return this;    
        }
        
        public Builder followRedirects(boolean followRedirects) {
            mPropertiesBuilder.followRedirects(followRedirects);
            return this;    
        }
        
        public Builder properties(RestProperties properties) {
            if (properties != null) {
                mPropertiesBuilder
                    .url(properties.getUrl())
                    .path(properties.getPath())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .connectTimeout(properties.getConnectTimeout())
                    .readTimeout(properties.getReadTimeout());
            }
            return this;    
        }
        
        public Builder authorizationType(String authorizationType) {
        	mAuthorizationType = authorizationType;
            return this;    
        }
        
        public Builder contentType(String contentType) {
            mContentType = contentType;
            return this;    
        }
        
        public Builder incomingCharset(String charset) {
            mIncomingCharset = charset;
            return this;    
        }
        
        public Builder outgoingCharset(String charset) {
            mOutgoingCharset = charset;
            return this;    
        }
        
        public Builder params(HashMap<String, String> params) {
            if (params != null) {
                mParams.putAll(params);
            }
            return this;    
        }
        
        public Builder param(String key, Object value) {
        	mParams.put(key, value == null ? "" : value);
            return this;    
        }
        
        public Builder param(String key, int value) {
            mParams.put(key, value);
            return this;      
        }
        
        public Builder param(String key, long value) {
            mParams.put(key, value);
            return this;     
        }
        
        public Builder param(String key, float value) {
            mParams.put(key, value);
            return this;      
        }
        
        public Builder param(String key, double value) {
            mParams.put(key, value);
            return this;      
        }
        
        public Builder param(String key, boolean value) {
            mParams.put(key, value);
            return this;     
        }
        
        public Builder cookies(List<String> cookies) {
            mCookies = cookies;
            return this;    
        }
        
        public String getEncodedQuery() throws UnsupportedEncodingException {
            StringBuilder query = new StringBuilder();
            if (!mParams.isEmpty()) {
                query.append(QUERY_SEPARATOR);
                query.append(RestUtils.toQuery(mParams, mOutgoingCharset));
            }
            return query.toString();
        }
        
        public RestConnection build() {
            final RestProperties properties = mPropertiesBuilder.build();
        	RestConnection connection = new RestConnection(new HttpURLConnectionFactory() {
                @Override
                public HttpURLConnection createHttpURLConnection(String method) throws MalformedURLException, UnsupportedEncodingException, IOException {
                    if (properties.getUrl() == null || properties.getUrl().isEmpty()) {
                        throw new MalformedURLException("You must call url(...) with a valid URL value!");
                    }
                    HttpURLConnection connection = (HttpURLConnection) new URL(properties.getCompleteUrl() + getEncodedQuery()).openConnection();
                    connection.setRequestMethod(method);
                    connection.setConnectTimeout(properties.getConnectTimeout());
                    connection.setReadTimeout(properties.getReadTimeout());
                    connection.setInstanceFollowRedirects(properties.isFollowRedirects());
                    connection.setRequestProperty(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                    connection.setRequestProperty(HEADER_ACCEPT_CHARSET, mOutgoingCharset);
                    if (properties.getUsername() != null && properties.getPassword() != null) {
                        String credentials = properties.getUsername() + ":" + properties.getPassword();
                        String authorization = mAuthorizationType + " " + Base64.encodeBytes(credentials.getBytes());
                        connection.setRequestProperty(HEADER_AUTHORIZATION, authorization);
                    }
                    if (mCookies != null) {
                        for (String cookie : mCookies) {
                            connection.addRequestProperty(HEADER_COOKIE, cookie);
                        }
                    }
                    if (mListener != null) {
                        mListener.onPrepareConnection(connection);
                    }
                    return connection;
                }
        	    
        	});
        	connection.mParser = mParser;
            connection.mMaxRetryAttempts = mMaxRetryAttempts;
            connection.mBackOffPolicy = mBackOffPolicy;
            connection.mRetryOnIOException = mRetryOnIOException;
        	connection.mContentType = mContentType;
        	connection.mIncomingCharset = mIncomingCharset;
        	connection.mOutgoingCharset = mOutgoingCharset;
        	return connection;
        }
    }
}

