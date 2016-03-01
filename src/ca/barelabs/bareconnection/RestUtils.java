package ca.barelabs.bareconnection;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class RestUtils {

    
    private RestUtils() {        
    }
    
    
    public static String buildQuery(Map<?, ?> params, String charset) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if(params != null) {
            Set<?> keySet = params.keySet();
            for (Object key : keySet) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                Object value = params.get(key);
                sb.append(key + "=" + URLEncoder.encode(value.toString(), charset));
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

    public static <T> T fromJson(InputStream in, Type type) throws IOException {
        return fromJson(in, RestConnection.DEFAULT_CHARSET, type);
    }

    public static <T> T fromJson(InputStream in, String charset, Type type) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), charset));
        return fromJson(reader, type);
    }

    public static <T> T fromJson(JsonReader reader, Type type) throws IOException {
        try {
            T object = new Gson().fromJson(reader, type);
            reader.close();
            return object;
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

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) { /* Swallow quietly */ }
    }
}
