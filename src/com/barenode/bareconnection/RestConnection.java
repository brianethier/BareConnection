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
package com.barenode.bareconnection;


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
        public void onWrite(OutputStream out, String charset, String boundary) throws IOException;
    }


    private HttpURLConnection mConnection;
    private Builder mBuilder;
    private String mContentType = CONTENT_TYPE_JSON;
    private String mIncomingCharset = DEFAULT_CHARSET;
    private String mOutgoingCharset = DEFAULT_CHARSET;
    private int mResponseCode = SC_UNKNOWN;


    public RestConnection(HttpURLConnection connection) {
        if(connection == null) {
            throw new IllegalStateException("A RestConnection must be called with a valid HttpURLConnection or use the Builder class!");
        }
        mConnection = connection;
    }

    private RestConnection(Builder builder) {
        mBuilder = builder;
    }


    public HttpURLConnection getConnection() throws RestException {
    	ensureConnection();
        return mConnection;
    }

    public void head() throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            mConnection.setRequestMethod(METHOD_HEAD);
            checkResponseCode();
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            mConnection.disconnect();
        }
    }

    public <T>T get(Class<T> clss) throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            mConnection.setRequestMethod(METHOD_GET);
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            if(!isResponseStreaming(clss)) {
                mConnection.disconnect();
            }
        }
    }

    public <T>List<T> getList(Class<T> clss) throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            mConnection.setRequestMethod(METHOD_GET);
            checkResponseCode();
            return RestUtils.fromJsonToList(mConnection.getInputStream(), mIncomingCharset, clss);
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            if(!isResponseStreaming(clss)) {
                mConnection.disconnect();
            }
        }
    }

    public void put() throws RestException {
        put(String.class, null);
    }

    public <T>T put(Class<T> clss, Object object) throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            String contentType = mContentType + ";" + KEY_CHARSET + "=" + mOutgoingCharset;
            mConnection.setRequestMethod(METHOD_PUT);
            mConnection.setRequestProperty(HEADER_CONTENT_TYPE, contentType); 
            mConnection.setDoOutput(true);
            write(mConnection.getOutputStream(), object);
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            if(!isResponseStreaming(clss)) {
                mConnection.disconnect();
            }
        }
    }

    public <T>T post(Class<T> clss, Object object) throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            String contentType = mContentType + ";" + KEY_CHARSET + "=" + mOutgoingCharset;
            mConnection.setRequestMethod(METHOD_POST);
            mConnection.setRequestProperty(HEADER_CONTENT_TYPE, contentType); 
            mConnection.setDoOutput(true);
            write(mConnection.getOutputStream(), object);
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            if(!isResponseStreaming(clss)) {
                mConnection.disconnect();
            }
        }
    }

    public <T>T post(Class<T> clss, Map<String, String> params) throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            String query = RestUtils.buildQuery(params, mOutgoingCharset);
            String contentType = CONTENT_TYPE_FORM_URLENCODED + ";" + KEY_CHARSET + "=" + mOutgoingCharset;
            mConnection.setRequestMethod(METHOD_POST);
            mConnection.setRequestProperty(HEADER_CONTENT_TYPE, contentType);
            mConnection.setDoOutput(true);
            write(mConnection.getOutputStream(), query);
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            if(!isResponseStreaming(clss)) {
                mConnection.disconnect();
            }
        }
    }

    public <T>T post(Class<T> clss, List<Entity> entities) throws RestException {
        return post(clss, new DefaultMultipartFormWriter(entities));
    }

    public <T>T post(Class<T> clss, MultipartFormWriter writer) throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            String boundary = Long.toHexString(System.currentTimeMillis());
            String contentType = CONTENT_TYPE_MULTIPART_FORM + ";" + KEY_BOUNDARY + "=" + boundary;
            mConnection.setRequestMethod(METHOD_POST);
            mConnection.setRequestProperty(HEADER_CONTENT_TYPE, contentType);
            mConnection.setDoOutput(true);
            OutputStream out = mConnection.getOutputStream();
            writer.onWrite(out, mOutgoingCharset, boundary);
            out.flush();
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            if(!isResponseStreaming(clss)) {
                mConnection.disconnect();
            }
        }
    }

    public <T>T delete(Class<T> clss) throws RestException {
    	ensureConnection();
        ensureNewConnection();
        try {
            mConnection.setRequestMethod(METHOD_DELETE);
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(mResponseCode, e);
        }
        finally {
            if(!isResponseStreaming(clss)) {
                mConnection.disconnect();
            }
        }
    }
    
    public List<String> getCookies() {
        if(mResponseCode == SC_UNKNOWN) {
            throw new IllegalStateException("A connection to the server needs to be made before retrieving cookies.");
        }
        ArrayList<String> cookies = new ArrayList<String>();
        List<String> incomingCookies = mConnection.getHeaderFields().get(HEADER_SET_COOKIE);
        for(String cookie : incomingCookies) {
            cookies.add(cookie.split(";", 2)[0]);
        }
        return cookies;
    }
    
    public int getResponseCode() {
        return mResponseCode;
    }

    public void disconnect() {
    	if(mConnection != null) {
    		mConnection.disconnect();
    	}
    }
    
    private void ensureConnection() throws RestException {
        if(mConnection == null) {
            try {
            	// Build a connection based on the values set in the Builder object
            	RestProperties properties = mBuilder.mProperties;
            	String authType = mBuilder.mAuthorizationType;
                HttpURLConnection connection = (HttpURLConnection) new URL(mBuilder.createURL()).openConnection();
                connection.setConnectTimeout(properties.getConnectTimeout());
                connection.setReadTimeout(properties.getReadTimeout());
                connection.setRequestProperty(HEADER_ACCEPT_CHARSET, mOutgoingCharset);
                if(properties.getUsername() != null && properties.getPassword() != null) {
                    String credentials = properties.getUsername() + ":" + properties.getPassword();
                    String authorization = authType + " " + Base64.encodeBytes(credentials.getBytes());
                    connection.setRequestProperty(HEADER_AUTHORIZATION, authorization);
                }
                List<String> cookies = mBuilder.mCookies;
                if(cookies != null) {
                    for(String cookie : cookies) {
                        connection.addRequestProperty(HEADER_COOKIE, cookie);
                    }
                }
                mConnection = connection;
            } catch (IOException e) {
                throw new RestException(SC_UNKNOWN, e);
            }
        }
    }
    
    private void ensureNewConnection() {
        if(mResponseCode != SC_UNKNOWN) {
            throw new IllegalStateException("A RestConnection could only be used once!");
        }
    }
    
    private void checkResponseCode() throws RestException {
        try {
            mResponseCode = mConnection.getResponseCode();
            if(mResponseCode / 100 != 2) {
                String responseError = RestUtils.readString(mConnection.getErrorStream(), mIncomingCharset);
                throw new RestException(mResponseCode, responseError);
            }
            updateIncomingCharset();
        } catch (IOException e) {
            throw new RestException(mResponseCode, e);
        }
    }
    
    private void updateIncomingCharset() {
        String contentType = mConnection.getHeaderField(HEADER_CONTENT_TYPE);
        for(String param : contentType.replace(" ", "").split(";")) {
            if(param.startsWith(KEY_CHARSET + "=")) {
                mIncomingCharset = param.split("=", 2)[1];
                break;
            }
        }
    }
    
    private boolean isResponseStreaming(Class<?> clss) {
        if(clss.isAssignableFrom(InputStream.class)) {
            return true;
        }
        if(clss.isAssignableFrom(JsonReader.class)) {
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T read(InputStream in, Class<T> clss) throws IOException {
        if(clss.isAssignableFrom(InputStream.class)) {
            return (T) in;
        }
        else if(clss.isAssignableFrom(JsonReader.class)) {
            return (T) new JsonReader(new InputStreamReader(in, mIncomingCharset));
        }
        else if(clss.isAssignableFrom(String.class)) {
            return (T) RestUtils.readString(in, mIncomingCharset);
        }
        else {
            return RestUtils.fromJson(in, mIncomingCharset, clss);
        }
    }

    private void write(OutputStream out, Object object) throws IOException {
        if(object instanceof InputStream) {
            RestUtils.copy((InputStream) object, out);
            out.flush();
            out.close();
        }
        else if(object instanceof String) {
            String data = (String) object;
            out.write(data.getBytes(mOutgoingCharset));
            out.flush();
            out.close();
        }
        else if(object != null) {
            String json = RestUtils.toJson(object);
            out.write(json.getBytes(mOutgoingCharset));
            out.flush();
            out.close();
        }
    }
    
    
    
    public static final class Builder {

        private RestProperties mProperties = new RestProperties();
        private String mAuthorizationType = AUTHORIZATION_TYPE_BASIC;
        private String mContentType = CONTENT_TYPE_JSON;
        private String mIncomingCharset = DEFAULT_CHARSET;
        private String mOutgoingCharset = DEFAULT_CHARSET;
        private HashMap<String, String> mParams;
        private List<String> mCookies;
        
        public Builder url(String url) {
            mProperties.url(url);
            return this;    
        }
        
        public Builder path(String path) {
            mProperties.path(path);
            return this;    
        }
        
        public Builder username(String username) {
            mProperties.username(username);
            return this;    
        }
        
        public Builder password(String password) {
            mProperties.password(password);
            return this;    
        }
        
        public Builder connectTimeout(int connectTimeout) {
            mProperties.connectTimeout(connectTimeout);
            return this;    
        }
        
        public Builder readTimeout(int readTimeout) {
            mProperties.readTimeout(readTimeout);
            return this;    
        }
        
        public Builder properties(RestProperties properties) {
            mProperties = properties == null ? new RestProperties() : properties;
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
        	RestConnection connection = new RestConnection(this);
        	connection.mContentType = mContentType;
        	connection.mIncomingCharset = mIncomingCharset;
        	connection.mOutgoingCharset = mOutgoingCharset;
        	return connection;
        }
        
        public String createURL() throws MalformedURLException, UnsupportedEncodingException {
            if(mProperties.getUrl() == null || mProperties.getUrl().isEmpty()) {
                throw new MalformedURLException("You must call url(...) with a valid URL value!");
            }
            StringBuilder url = new StringBuilder(mProperties.getUrl());
            if(mProperties.getPath() != null && !mProperties.getPath().isEmpty()) {
            	url.append(PATH_SEPARATOR);
            	url.append(mProperties.getPath());
            }
            if(mParams != null && !mParams.isEmpty()) {
            	url.append(QUERY_SEPARATOR);
            	url.append(RestUtils.buildQuery(mParams, mOutgoingCharset));
            }
            return url.toString();
        }
    }
}

