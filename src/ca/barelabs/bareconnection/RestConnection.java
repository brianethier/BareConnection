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


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

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
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    public static final String KEY_CHARSET = "charset";
    public static final String KEY_BOUNDARY = "boundary";

    public static final String AUTHORIZATION_TYPE_BASIC = "Basic";
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";

    public static final String PATH_SEPARATOR = "/";
    public static final String QUERY_SEPARATOR = "?";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    
    public interface MultipartFormWriter {
        void onWrite(OutputStream out, String charset, String boundary) throws IOException;
    }


    private final HttpURLConnectionFactory mFactory;
    private RestResponse mResponse;
    private String mContentType = CONTENT_TYPE_JSON;
    private String mIncomingCharset = DEFAULT_CHARSET;
    private String mOutgoingCharset = DEFAULT_CHARSET;


    public RestConnection(HttpURLConnectionFactory factory) {
        if (factory == null) {
            throw new IllegalStateException("RestConnection must be created with an HttpURLConnectionFactory. Also see RestConnection.Builder");
        }
        mFactory = factory;
    }


    public void head() throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureNewConnection();
        HttpURLConnection connection = mFactory.createHttpURLConnection(METHOD_HEAD);
        try {
            checkResponse(connection);
        } finally {
            disconnect();
        }
    }

    public void get() throws MalformedURLException, UnsupportedEncodingException, IOException {
        getDownloadAs(null);
    }

    public <T> T getDownloadAs(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return read(getDownloadAsStream(), clss);
        } finally {
            disconnect();
        }
    }

    public <T> List<T> getDownloadAsList(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return readList(getDownloadAsStream(), clss);
        } finally {
            disconnect();
        }
    }

    public InputStream getDownloadAsStream() throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureNewConnection();
        HttpURLConnection connection = mFactory.createHttpURLConnection(METHOD_GET);
        checkResponse(connection);
        return connection.getInputStream();
    }

    public void put() throws MalformedURLException, UnsupportedEncodingException, IOException {
        put(null);
    }

    public void put(Object object) throws MalformedURLException, UnsupportedEncodingException, IOException {
        putDownloadAs(object, null);
    }

    public <T> T putDownloadAs(Object object, Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return read(putDownloadAsStream(object), clss);
        } finally {
            disconnect();
        }
    }

    public <T> List<T> putDownloadAsList(Object object, Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return readList(putDownloadAsStream(object), clss);
        } finally {
            disconnect();
        }
    }

    public InputStream putDownloadAsStream(Object object) throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureNewConnection();
        String boundary = Long.toHexString(System.currentTimeMillis());
        HttpURLConnection connection = mFactory.createHttpURLConnection(METHOD_PUT);
        connection.setRequestProperty(HEADER_CONTENT_TYPE, encodeContentType(object, boundary)); 
        connection.setDoOutput(true);
        write(connection.getOutputStream(), object, boundary);
        checkResponse(connection);
        return connection.getInputStream();
    }

    public void post() throws MalformedURLException, UnsupportedEncodingException, IOException {
        post(null);
    }

    public void post(Object object) throws MalformedURLException, UnsupportedEncodingException, IOException {
        postDownloadAs(object, null);
    }

    public <T> T postDownloadAs(Object object, Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return read(postDownloadAsStream(object), clss);
        } finally {
            disconnect();
        }
    }

    public <T> List<T> postDownloadAsList(Object object, Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return readList(postDownloadAsStream(object), clss);
        } finally {
            disconnect();
        }
    }

    public <T> T postDownloadAs(List<Entity> entities, Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return read(postDownloadAsStream(entities), clss);
        } finally {
            disconnect();
        }
    }

    public <T> List<T> postDownloadAsList(List<Entity> entities, Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return readList(postDownloadAsStream(entities), clss);
        } finally {
            disconnect();
        }
    }

    public InputStream postDownloadAsStream(List<Entity> entities) throws MalformedURLException, UnsupportedEncodingException, IOException {
        return postDownloadAsStream(new DefaultMultipartFormWriter(entities));        
    }

    public InputStream postDownloadAsStream(Object object) throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureNewConnection();
        String boundary = Long.toHexString(System.currentTimeMillis());
        HttpURLConnection connection = mFactory.createHttpURLConnection(METHOD_POST);
        connection.setRequestProperty(HEADER_CONTENT_TYPE, encodeContentType(object, boundary)); 
        connection.setDoOutput(true);
        write(connection.getOutputStream(), object, boundary);
        checkResponse(connection);
        return connection.getInputStream();        
    }

    public void delete() throws MalformedURLException, UnsupportedEncodingException, IOException {
        deleteDownloadAs(null);
    }

    public <T> T deleteDownloadAs(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return read(deleteDownloadAsStream(), clss);
        } finally {
            disconnect();
        }
    }

    public <T> List<T> deleteDownloadAsList(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        try {
            return readList(deleteDownloadAsStream(), clss);
        } finally {
            disconnect();
        }
    }

    public InputStream deleteDownloadAsStream() throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureNewConnection();
        HttpURLConnection connection = mFactory.createHttpURLConnection(METHOD_DELETE);
        checkResponse(connection);
        return connection.getInputStream();
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

    public int getResponseCode() {
        return mResponse == null ? SC_UNKNOWN : mResponse.getResponseCode();
    }

    public HttpURLConnection getResponseConnection() {
        if (mResponse == null) {
            throw new IllegalStateException("One of the method calls must be made before you can access the response connection");
        }
        return mResponse.getConnection();
    }
    
    public List<String> getResponseCookies() {
        if (mResponse == null || mResponse.getResponseCode() == SC_UNKNOWN) {
            throw new IllegalStateException("A connection to the server needs to be made before retrieving cookies.");
        }
        ArrayList<String> cookies = new ArrayList<String>();
        List<String> incomingCookies = mResponse.getConnection().getHeaderFields().get(HEADER_SET_COOKIE);
        for (String cookie : incomingCookies) {
            cookies.add(cookie.split(";", 2)[0]);
        }
        return cookies;
    }

    public void disconnect() {
        if (mResponse != null) {
            mResponse.getConnection().disconnect();
        }
    }
    
    private void ensureNewConnection() {
        if (mResponse != null) {
            throw new IllegalStateException("A RestConnection could only be used once!");
        }
    }
    
    private void checkResponse(HttpURLConnection connection) throws IOException {
        int responseCode = connection.getResponseCode();
        mResponse = new RestResponse(connection, responseCode);
        if (responseCode / 100 != 2) {
            String responseError = RestUtils.readString(connection.getErrorStream(), mIncomingCharset);
            throw new RestException(responseCode, responseError);
        }
        String contentType = connection.getHeaderField(HEADER_CONTENT_TYPE);
        for (String param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith(KEY_CHARSET + "=")) {
                mIncomingCharset = param.split("=", 2)[1];
                break;
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
    
    private <T> T read(InputStream in, Class<T> clss) throws IOException {
        try {
            if (clss != null) {
                return RestUtils.fromJson(in, mIncomingCharset, clss);
            }
            return null;
        } finally {
            RestUtils.closeQuietly(in);
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> List<T> readList(InputStream in, Class<T> clss) throws IOException {
        try {
            List<T> list = new ArrayList<T>();
            if (clss != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), mIncomingCharset));
                reader.beginArray();
                while (reader.hasNext()) {
                    list.add((T) RestUtils.fromJson(reader, clss));
                }
                reader.endArray();
                reader.close();
            }
            return list;
        } catch(JsonParseException e) {
            throw new IOException(e);
        } finally {
            RestUtils.closeQuietly(in);
        }
    }

    private void write(OutputStream out, Object object, String boundary) throws IOException {
        if (object instanceof InputStream) {
            RestUtils.copy((InputStream) object, out);
            out.flush();
            out.close();
        } else if (object instanceof String) {
            String data = (String) object;
            out.write(data.getBytes(mOutgoingCharset));
            out.flush();
            out.close();
        } else if (object instanceof Map) {
            String query = RestUtils.buildQuery((Map<?,?>) object, mOutgoingCharset);
            out.write(query.getBytes(mOutgoingCharset));
            out.flush();
            out.close();
        } else if (object instanceof MultipartFormWriter) {
            MultipartFormWriter writer = (MultipartFormWriter) object;
            writer.onWrite(out, mOutgoingCharset, boundary);
            out.flush();
            out.close();
        } else if (object != null) {
            String json = RestUtils.toJson(object);
            out.write(json.getBytes(mOutgoingCharset));
            out.flush();
            out.close();
        }
    }
    
    
    public static final class Builder {

        public interface OnPrepareConnectionListener {
            void onPrepareConnection(HttpURLConnection connection);
        }

        private RestProperties.Builder mPropertiesBuilder = new RestProperties.Builder();
        private String mAuthorizationType = AUTHORIZATION_TYPE_BASIC;
        private String mContentType = CONTENT_TYPE_JSON;
        private String mIncomingCharset = DEFAULT_CHARSET;
        private String mOutgoingCharset = DEFAULT_CHARSET;
        private HashMap<String, String> mParams;
        private List<String> mCookies;
        private OnPrepareConnectionListener mListener;        
        
        public Builder listener(OnPrepareConnectionListener listener) {
            mListener = listener;
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
        	mParams = params;
            return this;    
        }
        
        public Builder param(String key, String value) {
        	if(mParams == null) {
        		mParams = new HashMap<String, String>();
        	}
        	mParams.put(key, value);
            return this;    
        }
        
        public Builder cookies(List<String> cookies) {
            mCookies = cookies;
            return this;    
        }
        
        public RestConnection build() {
            final RestProperties properties = mPropertiesBuilder.build();
        	RestConnection connection = new RestConnection(new HttpURLConnectionFactory() {
                @Override
                public HttpURLConnection createHttpURLConnection(String method) throws MalformedURLException, UnsupportedEncodingException, IOException {
                    HttpURLConnection connection = (HttpURLConnection) new URL(createURL(properties)).openConnection();
                    connection.setRequestMethod(method);
                    connection.setConnectTimeout(properties.getConnectTimeout());
                    connection.setReadTimeout(properties.getReadTimeout());
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
        	connection.mContentType = mContentType;
        	connection.mIncomingCharset = mIncomingCharset;
        	connection.mOutgoingCharset = mOutgoingCharset;
        	return connection;
        }
        
        private String createURL(RestProperties properties) throws MalformedURLException, UnsupportedEncodingException {
            if (properties.getUrl() == null || properties.getUrl().isEmpty()) {
                throw new MalformedURLException("You must call url(...) with a valid URL value!");
            }
            StringBuilder url = new StringBuilder(properties.getUrl());
            if (properties.getPath() != null && !properties.getPath().isEmpty()) {
                if (!properties.getUrl().endsWith(PATH_SEPARATOR) && !properties.getPath().startsWith(PATH_SEPARATOR)) {
                    url.append(PATH_SEPARATOR);
                }
                url.append(properties.getPath());
            }
            if (mParams != null && !mParams.isEmpty()) {
                url.append(QUERY_SEPARATOR);
                url.append(RestUtils.buildQuery(mParams, mOutgoingCharset));
            }
            return url.toString();
        }
    }
}

