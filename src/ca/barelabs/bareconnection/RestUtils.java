package ca.barelabs.bareconnection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

public class RestUtils {    
    
    public static String toQuery(Map<?, ?> params, String charset) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
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
}
