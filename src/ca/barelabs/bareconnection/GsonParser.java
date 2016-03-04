package ca.barelabs.bareconnection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;


public class GsonParser extends ObjectParser {
    
    private final Gson mGson;
    
    
    public GsonParser() {
        this(new Gson());
    }
    
    public GsonParser(Gson gson) {
        mGson = gson;
    }

    <T> T parseAndClose(InputStream in, String charset, Class<T> clss) throws IOException {
        try {
            if (clss == null) {
                return null;
            }
            JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), charset));
            return mGson.fromJson(reader, clss);
        } catch(JsonParseException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    <T> List<T> parseListAndClose(InputStream in, String charset, Class<T> clss) throws IOException {
        try {
            List<T> list = new ArrayList<T>();
            if (clss != null) {
                JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), charset));
                reader.beginArray();
                while (reader.hasNext()) {
                    T next = mGson.fromJson(reader, clss);
                    list.add(next);
                }
                reader.endArray();
            }
            return list;
        } catch(JsonParseException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    void saveAndClose(Object object, OutputStream out, String charset) throws IOException {
        try {
            if (object != null) {
                String json = mGson.toJson(object);
                out.write(json.getBytes(charset));
                out.flush();
            }
        } finally {
            IOUtils.closeQuietly(out);
        }
    }
}
