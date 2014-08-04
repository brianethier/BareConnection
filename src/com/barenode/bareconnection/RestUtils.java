package com.barenode.bareconnection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class RestUtils {

    
	private RestUtils() {		
	}
	
    
    public static String buildQuery(HashMap<String, String> params, String charset) {
    	StringBuilder sb = new StringBuilder();
    	if(params != null) {
			try {
    		Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
	    		while(iterator.hasNext()) {
	    			Entry<String, String> entry = iterator.next();
	    			sb.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), charset));
	    			if(iterator.hasNext()) {
	    				sb.append("&");
	    			}
	    		}
			} catch (UnsupportedEncodingException e) {
				return "";
			}
    	}
    	return sb.toString();
    }

    public static String readString(InputStream is, String charset) throws UnsupportedEncodingException {
    	try {
	        Scanner s = new Scanner(is, charset);
	        s.useDelimiter("\\A");
	        String data = s.hasNext() ? s.next() : "";
	        s.close();
	        return data;
    	} catch(IllegalArgumentException e) {
    		throw new UnsupportedEncodingException(e.getMessage());
    	}
    }

    public static String toJson(Object src) {
        return src == null ? null : new Gson().toJson(src);
    }

    public static <T> T fromJson(String json, Class<T> clss) {
        return json == null ? null : new Gson().fromJson(json, clss);
    }

    public static <T> T fromJson(InputStream in, Class<T> clss, String charset) throws JsonParseException, IOException {
    	JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), charset));
    	T object = new Gson().fromJson(reader, clss);
    	reader.close();
    	return object;
    }
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
    	byte[] bytes = new byte[4096];
    	int totalRead = 0;
    	while((totalRead = in.read(bytes)) > 0) {
    		out.write(bytes, 0, totalRead);;
    		out.flush();
    	}
    	out.close();
    }
    
}
