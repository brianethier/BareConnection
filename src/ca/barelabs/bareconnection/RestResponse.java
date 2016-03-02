/*
 * Copyright 2016 Brian Ethier
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import ca.barelabs.barechatservice.utils.IOUtils;

import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;


public class RestResponse {
    
    private final HttpURLConnection mConnection;
    private final int mStatusCode;
    private final String mIncomingCharset;
    private InputStream mContent;

    RestResponse(HttpURLConnection connection, String incomingCharset) throws IOException {
        mConnection = connection;
        mStatusCode = connection.getResponseCode();
        mIncomingCharset = parseIncomingCharset(connection, incomingCharset);
    }
    
    public HttpURLConnection getConnection() {
        return mConnection;
    }
    
    public int getStatusCode() {
        return mStatusCode;
    }
    
    public String getIncomingCharset() {
        return mIncomingCharset;
    }
    
    public List<String> getCookies() {
        List<String> cookies = new ArrayList<String>();
        List<String> incomingCookies = mConnection.getHeaderFields().get(RestConnection.HEADER_SET_COOKIE);
        for (String cookie : incomingCookies) {
            cookies.add(cookie.split(";", 2)[0]);
        }
        return cookies;
    }
    
    public InputStream getContent() throws IOException {
        ensureValidStatusCode();
        if (mContent == null) {
            mContent = mConnection.getInputStream();
        }
        return mContent;
    }

    public String parse() throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureValidStatusCode();
        try {
            return RestUtils.readString(getContent(), mIncomingCharset);
        } finally {
            disconnect();
        }
    }

    public <T> T parseAs(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureValidStatusCode();
        try {
            if (clss == null) {
                return null;
            }
            return RestUtils.fromJson(getContent(), mIncomingCharset, clss);
        } finally {
            disconnect();
        }
    }

    public <T> List<T> parseAsList(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureValidStatusCode();
        try {
            List<T> list = new ArrayList<T>();
            if (clss != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(getContent()), mIncomingCharset));
                reader.beginArray();
                while (reader.hasNext()) {
                    T next = RestUtils.fromJson(reader, clss);
                    list.add(next);
                }
                reader.endArray();
                reader.close();
            }
            return list;
        } catch(JsonParseException e) {
            throw new IOException(e);
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            IOUtils.closeQuietly(getContent());
        } catch (IOException e) { /* Ignore exception, attempting to disconnect. */ }
        mConnection.disconnect();
    }
    
    private String parseIncomingCharset(HttpURLConnection connection, String definedIncomingCharset) {
        String contentType = connection.getHeaderField(RestConnection.HEADER_CONTENT_TYPE);
        for (String param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith(RestConnection.KEY_CHARSET + "=")) {
                return param.split("=", 2)[1];
            }
        }
        return definedIncomingCharset;
    }
    
    private void ensureValidStatusCode() throws IOException {
        if (mStatusCode / 100 != 2) {
            String responseError = RestUtils.readString(mConnection.getErrorStream(), mIncomingCharset);
            throw new RestException(mStatusCode, responseError);
        }
    }
}
