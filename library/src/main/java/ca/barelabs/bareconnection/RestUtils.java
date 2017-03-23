package ca.barelabs.bareconnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;


public class RestUtils {

	public static String toPath(String... paths) {
		StringBuilder sb = new StringBuilder();
        if (paths != null) {
        	for (String path : paths) {
        		if (path == null) {
        			continue;
        		}
                if (sb.length() > 0) {
                    sb.append(RestConnection.PATH_SEPARATOR);
                }
                sb.append(path);
        	}
        }
        return sb.toString();
	}
    
    public static <K,V> String toQuery(Map<K,V> params, String charset) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (K key : params.keySet()) {
            	if (key != null) {
                	appendToQuery(sb, key, params.get(key), charset);
            	}
            }
        }
        return sb.toString();
    }
    
    public static <K,V> String toQuery(MultiMap<K,V> params, String charset) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        if (params != null) {
            for (K key : params.keySet()) {
            	if (key != null) {
                    List<V> values = params.get(key);
                    if (values == null || values.isEmpty()) {
                    	appendToQuery(sb, key, null, charset);
                    } else {
	                    for (V value : values) {
	                    	appendToQuery(sb, key, value, charset);
	                    }
                    }
            	}
            }
        }
        return sb.toString();
    }
    
    public static <K,V> void appendToQuery(StringBuilder sb, K key, V value, String charset) throws UnsupportedEncodingException {
        if (sb != null && key != null) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(URLEncoder.encode(key.toString(), charset) + "=");
            if (value != null) {
            	sb.append(URLEncoder.encode(value.toString(), charset));
            }
        }
    }
}
