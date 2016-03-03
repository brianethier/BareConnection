package ca.barelabs.bareconnection;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;


public class IOUtils {

    public static String toString(InputStream is, String charset) throws IOException {
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
    
    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] bytes = new byte[4096];
        int totalRead = 0;
        while ((totalRead = in.read(bytes)) > 0) {
            out.write(bytes, 0, totalRead);
            out.flush();
        }
        in.close();
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
            }
        }
    }
}
