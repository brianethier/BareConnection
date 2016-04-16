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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class RestResponse {
    
    private final HttpURLConnection mConnection;
    private final ObjectParser mParser;
    private final int mStatusCode;
    private final String mIncomingCharset;
    private InputStream mContent;

    RestResponse(HttpURLConnection connection, ObjectParser parser, String incomingCharset) throws IOException {
        mConnection = connection;
        mParser = parser;
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
    
    public InputStream getEncodedContent() throws IOException {
        ensureValidStatusCode();
        if (mContent == null) {
            mContent = mConnection.getInputStream();
        }
        return mContent;
    }
    
    public InputStream getContent() throws IOException {
        ensureValidStatusCode();
        if (mContent == null) {
            mContent = decodeStream(mConnection.getInputStream());
        }
        return mContent;
    }

    public String parse() throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureValidStatusCode();
        try {
            return IOUtils.toString(getContent(), mIncomingCharset);
        } finally {
            disconnect();
        }
    }

    public <T> T parseAs(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureValidStatusCode();
        try {
        	if (mParser == null) {
                throw new IllegalStateException("Missing ObjectParser. See RestConnection.setParser() or include Gson dependency to default to GsonParser.");
        	}
            return mParser.parseAndClose(getContent(), mIncomingCharset, clss);
        } finally {
            disconnect();
        }
    }

    public <T> List<T> parseAsList(Class<T> clss) throws MalformedURLException, UnsupportedEncodingException, IOException {
        ensureValidStatusCode();
        try {
        	if (mParser == null) {
                throw new IllegalStateException("Missing ObjectParser. See RestConnection.setParser() or include Gson dependency to default to GsonParser.");
        	}
            return mParser.parseListAndClose(getContent(), mIncomingCharset, clss);
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
    
    private InputStream decodeStream(InputStream in) throws IOException {
        String encoding = mConnection.getContentEncoding();
        boolean gzipped = encoding != null && encoding.equalsIgnoreCase(RestConnection.ENCODING_GZIP);
        return gzipped ? new GZIPInputStream(in) : in;
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
            String responseError = IOUtils.toString(decodeStream(mConnection.getErrorStream()), mIncomingCharset);
            throw new RestException(mStatusCode, responseError);
        }
    }
}
