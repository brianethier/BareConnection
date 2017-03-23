package ca.barelabs.bareconnection;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;

public class GsonParser implements ObjectParser {
    
    private final Gson mGson;
    
    public GsonParser() {
        this(new Gson());
    }
    
    public GsonParser(Gson gson) {
        mGson = gson;
    }

    public Gson getGson() {
        return mGson;
    }

    @Override
    public <T> T parse(String value, Type type) throws IOException {
        try {
            if (type == null) {
                return null;
            }
            return mGson.fromJson(value, type);
        } catch(JsonParseException e) {
            throw new IOException(e);
        }
    }

    @Override
    public <T> T parseAndClose(InputStream in, String charset, Type type) throws IOException {
        try {
            if (type == null) {
                return null;
            }
            JsonReader reader = new JsonReader(new InputStreamReader(new BufferedInputStream(in), charset));
            return mGson.fromJson(reader, type);
        } catch(JsonParseException e) {
            throw new IOException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public void saveAndClose(Object object, OutputStream out, String charset) throws IOException {
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
