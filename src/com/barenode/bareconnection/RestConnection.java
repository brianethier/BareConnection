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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.JsonParseException;

public class RestConnection {

	public static final int HTTP_UNKNOWN_CODE = -1;
	public static final int HTTP_OK = 200;
	public static final int HTTP_CREATED = 201;
	public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
	
	public static final String CHARSET = "UTF-8";
	public static final String CRLF = "\r\n";


    private final HttpURLConnection mConnection;
	private String mIncomingCharset = CHARSET;
	private String mOutgoingCharset = CHARSET;
    private int mResponseCode = HTTP_UNKNOWN_CODE;


    public RestConnection(HttpURLConnection connection) {
        mConnection = connection;
    }


    public HttpURLConnection getConnection() {
        return mConnection;
    }

    public void head() throws RestException {
    	ensureNewConnection();
        try {
            mConnection.setRequestMethod("HEAD");
            checkResponseCode();
        }
        catch(IOException e) {
            throw new RestException(e);
        }
        finally {
            mConnection.disconnect();
        }
    }

	public <T>T get(Class<T> clss) throws RestException {
		ensureNewConnection();
        try {
            mConnection.setRequestMethod("GET");
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(e);
        }
        finally {
            if(!clss.isAssignableFrom(InputStream.class)) {
                mConnection.disconnect();
            }
        }
    }

	public void put() throws RestException {
		put(null, String.class);
    }

	public <T>T put(Object object, Class<T> clss) throws RestException {
		ensureNewConnection();
        try {
            mConnection.setRequestMethod("PUT");
	        mConnection.setDoOutput(true);
            write(mConnection.getOutputStream(), object);
        	checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(e);
        }
        finally {
            if(!clss.isAssignableFrom(InputStream.class)) {
                mConnection.disconnect();
            }
        }
    }

	public <T>T post(Object object, Class<T> clss) throws RestException {
		ensureNewConnection();
        try {
            mConnection.setRequestMethod("POST");
	        mConnection.setDoOutput(true);
            write(mConnection.getOutputStream(), object);
        	checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(e);
        }
        finally {
            if(!clss.isAssignableFrom(InputStream.class)) {
                mConnection.disconnect();
            }
        }
    }

	public <T>T post(HashMap<String, String> params, Class<T> clss) throws RestException {
		ensureNewConnection();
        try {
        	String query = RestUtils.buildQuery(params, mOutgoingCharset);
            mConnection.setRequestMethod("POST");
            mConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + mOutgoingCharset);
	        mConnection.setDoOutput(true);
	        write(mConnection.getOutputStream(), query);
        	checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(e);
        }
        finally {
            if(!clss.isAssignableFrom(InputStream.class)) {
                mConnection.disconnect();
            }
        }
    }

	public <T>T post(List<Entity> entities, Class<T> clss) throws RestException {
		ensureNewConnection();
        try {
	        String boundary = Long.toHexString(System.currentTimeMillis());
            mConnection.setRequestMethod("POST");
	        mConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    		mConnection.setDoOutput(true);
            writeEntities(mConnection.getOutputStream(), entities, boundary);
        	checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(e);
        }
        finally {
            if(!clss.isAssignableFrom(InputStream.class)) {
                mConnection.disconnect();
            }
        }
    }

	public <T>T delete(Class<T> clss) throws RestException {
		ensureNewConnection();
        try {
            mConnection.setRequestMethod("DELETE");
            checkResponseCode();
            return read(mConnection.getInputStream(), clss);
        }
        catch(IOException e) {
            throw new RestException(e);
        }
        finally {
            if(!clss.isAssignableFrom(InputStream.class)) {
                mConnection.disconnect();
            }
        }
    }
	
	public List<String> getCookies() {
		if(mResponseCode == HTTP_UNKNOWN_CODE) {
    		throw new IllegalStateException("A connection to the server needs to be made before retrieving cookies.");
		}
		ArrayList<String> cookies = new ArrayList<String>();
		List<String> incomingCookies = mConnection.getHeaderFields().get("Set-Cookie");
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
    	if(mResponseCode != HTTP_UNKNOWN_CODE) {
    		throw new IllegalStateException("A RestConnection could only be used once!");
    	}
    }
    
    private void checkResponseCode() throws RestException {
		try {
			mResponseCode = mConnection.getResponseCode();
	        if(mResponseCode / 100 != 2) {
	            throw new RestException(mResponseCode, RestUtils.readString(mConnection.getErrorStream(), mIncomingCharset));
	        }
	        updateIncomingCharset();
		} catch (IOException e) {
            throw new RestException(e);
		}
    }
    
    private void updateIncomingCharset() {
    	String contentType = mConnection.getHeaderField("Content-Type");
    	for(String param : contentType.replace(" ", "").split(";")) {
    	    if(param.startsWith("charset=")) {
    	        mIncomingCharset = param.split("=", 2)[1];
    	        break;
    	    }
    	}
    }
	
    private void setIncomingCharset(String charset) {
		mIncomingCharset = charset;
	}
	
	private void setOutgoingCharset(String charset) {
		mOutgoingCharset = charset;
	}
    
    @SuppressWarnings("unchecked")
	private <T> T read(InputStream in, Class<T> clss) throws IOException, RestException {
    	try {
            if(clss.isAssignableFrom(InputStream.class)) {
                return (T) in;
            }
            else if(clss.isAssignableFrom(String.class)) {
                return (T) RestUtils.readString(in, mIncomingCharset);
            }
            else {
            	return RestUtils.fromJson(in, clss, mIncomingCharset);
            }
        }
    	catch(UnsupportedEncodingException e) {
            throw new RestException(HTTP_INTERNAL_SERVER_ERROR, e);
    	}
        catch(JsonParseException e) {
            throw new RestException(HTTP_INTERNAL_SERVER_ERROR, e);
        }
    }

    private void write(OutputStream out, Object object) throws IOException {
        if(object instanceof InputStream) {
            RestUtils.copy((InputStream) object, out);
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
    
    private void writeEntities(OutputStream out, List<Entity> entities, String boundary) throws IOException {
    	if(entities != null) {
    		PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, mOutgoingCharset), true);
    		for(Entity entity : entities) {
    			if(entity instanceof StringEntity) {
    				StringEntity stringEntity = (StringEntity) entity;
    		        writer.append("--" + boundary).append(CRLF);
    		        writer.append("Content-Disposition: form-data; name=\"" + stringEntity.name + "\"").append(CRLF);
    		        writer.append("Content-Type: text/plain; charset=" + stringEntity.charset).append(CRLF);
    		        writer.append(CRLF).append(stringEntity.value).append(CRLF).flush();
    			}
    			else if(entity instanceof FileEntity) {
    				FileEntity fileEntity = (FileEntity) entity;
    		        writer.append("--" + boundary).append(CRLF);
    		        writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + fileEntity.name + "\"").append(CRLF);
    		        writer.append("Content-Type: text/plain; charset=" + fileEntity.charset).append(CRLF); // Text file itself must be saved in this charset!
    		        writer.append(CRLF).flush();
    	            RestUtils.copy(new FileInputStream(fileEntity.file), out);
    		        out.flush(); // Important before continuing with writer!
    		        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    			}
    			else if(entity instanceof BinaryFileEntity) {
    				BinaryFileEntity fileEntity = (BinaryFileEntity) entity;
    		        writer.append("--" + boundary).append(CRLF);
    		        writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + fileEntity.name + "\"").append(CRLF);
    		        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileEntity.file.getName())).append(CRLF);
    		        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
    		        writer.append(CRLF).flush();
    	            RestUtils.copy(new FileInputStream(fileEntity.file), out);
    		        out.flush(); // Important before continuing with writer!
    		        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
    			}
    		}
            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF);
    	}
    }
    
    
    
    public static abstract class Entity {

    	protected final String name;
    	protected final String charset;
    	
    	private Entity(String name) {
    		this(name, CHARSET);
    	}
    	
    	private Entity(String name, String charset) {
    		this.name = name;
    		this.charset = charset;
    	}
    }
    
    public static final class StringEntity extends Entity {
    	
    	private final String value;
    	
    	public StringEntity(String name, String value) {
    		this(name, value, CHARSET);
    	}
    	
    	public StringEntity(String name, String value, String charset) {
    		super(name, charset);
    		this.value = value;
    	}
    }
    
    public static final class FileEntity extends Entity {
    	
    	private final File file;
    	
    	public FileEntity(String name, File file) {
    		this(name, file, CHARSET);
    	}
    	
    	public FileEntity(String name, File file, String charset) {
    		super(name, charset);
    		this.file = file;
    	}
    }
    
    public static final class BinaryFileEntity extends Entity {
    	
    	private final File file;
    	
    	public BinaryFileEntity(String name, File file) {
    		this(name, file, CHARSET);
    	}
    	
    	public BinaryFileEntity(String name, File file, String charset) {
    		super(name, charset);
    		this.file = file;
    	}
    }
    
    
    public static final class Builder {

    	private RestProperties mProperties = new RestProperties();
    	private String mIncomingCharset = CHARSET;
    	private String mOutgoingCharset = CHARSET;
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
    		if(properties != null) {
    			mProperties = properties;
    		}
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
    	
    	public Builder path(String path, HashMap<String, String> params) {
        	mPath = params == null ? path : path + "?" + RestUtils.buildQuery(params, mOutgoingCharset);
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
    			connection.setRequestProperty("Accept-Charset", mOutgoingCharset);
	    		if(mProperties.getUsername() != null && mProperties.getPassword() != null) {
	    			connection.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary((mProperties.getUsername() +":"+ mProperties.getPassword()).getBytes()));
	    		}
	    		if(mCookies != null) {
		    		for(String cookie : mCookies) {
		    		    connection.addRequestProperty("Cookie", cookie);
		    		}
	    		}
	    		RestConnection restConnection = new RestConnection(connection);
	    		restConnection.setIncomingCharset(mIncomingCharset);
	    		restConnection.setOutgoingCharset(mOutgoingCharset);
	    		return restConnection;
			} catch (MalformedURLException e) {
				throw new RestException(e);
			} catch (IOException e) {
				throw new RestException(e);
			}
    	}
    	
    	private String createFullUrl() {
    		if(mPath == null || mPath.isEmpty()) {
    			return mProperties.getUrl();
    		}
    		if(mPath.startsWith("/")) {
    			return mProperties.getUrl() + mPath;
    		}
    		return mProperties.getUrl() + "/" + mPath;
    	}
    }
}

