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
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_PRECONDITION_FAILED = 412;
    public static final int SC_SERVER_ERROR = 500;

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_SET_COOKIE = "Set-Cookie";
    public static final String HEADER_COOKIE = "Cookie";
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    
    public static final String KEY_CHARSET = "charset";
    public static final String KEY_BOUNDARY = "boundary";
    
    public static final String CONTENT_TYPE_TEXT = "text/plain";
    public static final String CONTENT_TYPE_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_MULTIPART_FORM = "multipart/form-data";
    
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int DEFAULT_CONNECT_TIMEOUT = 1000;
    public static final int DEFAULT_SOCKET_TIMEOUT = 10000;
    
    public interface MultipartFormWriter {
        public void onWrite(OutputStream out, String charset, String boundary) throws IOException;
    }


    private final HttpURLConnection mConnection;
    private String mContentType = CONTENT_TYPE_JSON;
    private String mIncomingCharset = DEFAULT_CHARSET;
    private String mOutgoingCharset = DEFAULT_CHARSET;
    private int mResponseCode = SC_UNKNOWN;


    public RestConnection(HttpURLConnection connection) {
        mConnection = connection;
    }


    public HttpURLConnection getConnection() {
        return mConnection;
    }

    public void head() throws RestException {
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

    public <T>T post(Class<T> clss, HashMap<String, String> params) throws RestException {
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
        mConnection.disconnect();
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
    
    private void setContentType(String contentType) {
        mContentType = contentType;
    }
    
    private void setIncomingCharset(String charset) {
        mIncomingCharset = charset;
    }
    
    private void setOutgoingCharset(String charset) {
        mOutgoingCharset = charset;
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
        private String mContentType = CONTENT_TYPE_JSON;
        private String mIncomingCharset = DEFAULT_CHARSET;
        private String mOutgoingCharset = DEFAULT_CHARSET;
        private String mPath;
        private List<String> mCookies;
        
        public Builder url(String url) {
            mProperties.url(url);
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
        
        public Builder path(String path) {
            mPath = path;
            return this;    
        }
        
        public Builder path(String path, HashMap<String, String> params) throws RestException {
            try {
                mPath = params == null ? path : path + "?" + RestUtils.buildQuery(params, mOutgoingCharset);
            } catch (UnsupportedEncodingException e) {
                throw new RestException(SC_UNKNOWN, e);
            }
            return this;    
        }
        
        public Builder cookies(List<String> cookies) {
            mCookies = cookies;
            return this;    
        }
        
        public RestConnection build() throws RestException {
            try {
                String url = createFullUrl();
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(mProperties.getConnectTimeout());
                connection.setReadTimeout(mProperties.getReadTimeout());
                connection.setRequestProperty(HEADER_ACCEPT_CHARSET, mOutgoingCharset);
                if(mProperties.getUsername() != null && mProperties.getPassword() != null) {
                    connection.setRequestProperty(HEADER_AUTHORIZATION, createBasicAuthorization());
                }
                if(mCookies != null) {
                    for(String cookie : mCookies) {
                        connection.addRequestProperty(HEADER_COOKIE, cookie);
                    }
                }
                RestConnection restConnection = new RestConnection(connection);
                restConnection.setContentType(mContentType);
                restConnection.setIncomingCharset(mIncomingCharset);
                restConnection.setOutgoingCharset(mOutgoingCharset);
                return restConnection;
            } catch (MalformedURLException e) {
                throw new RestException(SC_UNKNOWN, e);
            } catch (IOException e) {
                throw new RestException(SC_UNKNOWN, e);
            }
        }
        
        private String createFullUrl() throws MalformedURLException {
            if(mProperties.getUrl() == null || mProperties.getUrl().isEmpty()) {
                throw new MalformedURLException("You must call url(...) with a valid URL value!");
            }
            if(mPath == null || mPath.isEmpty()) {
                return mProperties.getUrl();
            }
            if(mPath.startsWith("/")) {
                return mProperties.getUrl() + mPath;
            }
            return mProperties.getUrl() + "/" + mPath;
        }
        
        private String createBasicAuthorization() {
            String credentials = mProperties.getUsername() + ":" + mProperties.getPassword();
            return "Basic " + Base64.encodeBytes(credentials.getBytes());
        }
    }
}

