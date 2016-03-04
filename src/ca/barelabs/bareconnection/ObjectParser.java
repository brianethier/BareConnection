package ca.barelabs.bareconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class ObjectParser {
    
    public static final String MISSING_PARSER_ERROR = "Missing ObjectParser. See RestConnection.setParser() / RestConnection.Builder.parser() or include Gson dependency to default to GsonParser.";
    
    static ObjectParser getDefault() {
        try {
            // See if Gson dependency is present. If so use that as default.
            Class.forName("com.google.gson.Gson", false, ObjectParser.class.getClassLoader());
            return new GsonParser();
        } catch(ClassNotFoundException e) {
            return new ObjectParser();
        }
    }
    
    protected ObjectParser() {        
    }

    <T> T parseAndClose(InputStream in, String charset, Class<T> clss) throws IOException {
        throw new IllegalStateException(MISSING_PARSER_ERROR);
    }

    <T> List<T> parseListAndClose(InputStream in, String charset, Class<T> clss) throws IOException {
        throw new IllegalStateException(MISSING_PARSER_ERROR);
    }

    void saveAndClose(Object object, OutputStream out, String charset) throws IOException {
        throw new IllegalStateException(MISSING_PARSER_ERROR);
    }
}
