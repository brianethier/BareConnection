package com.barenode.bareconnection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class RestUtils {

    
    private RestUtils() {        
    }
    
    
    public static String buildQuery(Map<String, String> params, String charset) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if(params != null) {
            Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext()) {
                Entry<String, String> entry = iterator.next();
                sb.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), charset));
                if(iterator.hasNext()) {
                    sb.append("&");
                }
            }
        }
        return sb.toString();
    }

    public static String readString(InputStream is, String charset) throws IOException {
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

    public static <T> T fromJson(InputStream in, Class<T> clss) throws IOException {
        return fromJson(in, RestConnection.DEFAULT_CHARSET, clss);
    }

    public static <T> T fromJson(InputStream in, String charset, Class<T> clss) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), charset));
        return fromJson(reader, clss);
    }

    public static <T> T fromJson(JsonReader reader, Class<T> clss) throws IOException {
        try {
            T object = new Gson().fromJson(reader, clss);
            reader.close();
            return object;
        }
        catch(JsonParseException e) {
            throw new IOException(e);
        }
    }
    
    public static <T> List<T> fromJsonToList(InputStream in, Class<T> clss) throws IOException {
        return fromJsonToList(in, RestConnection.DEFAULT_CHARSET, clss);
    }
    
    public static <T> List<T> fromJsonToList(InputStream in, String charset, Class<T> clss) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), charset));
        return fromJsonToList(reader, clss);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> fromJsonToList(JsonReader reader, Class<T> clss) throws IOException {
        try {
            ArrayList<T> list = new ArrayList<T>();
            Gson gson = new Gson();
            reader.beginArray();
            while(reader.hasNext()) {
                list.add((T) gson.fromJson(reader, clss));
            }
            reader.endArray();
            reader.close();
            return list;
        }
        catch(JsonParseException e) {
            throw new IOException(e);
        }
    }
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[4096];
        int totalRead = 0;
        while((totalRead = in.read(bytes)) > 0) {
            out.write(bytes, 0, totalRead);;
            out.flush();
        }
        in.close();
    }
    
}
