package ca.barelabs.bareconnection;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;


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
}
