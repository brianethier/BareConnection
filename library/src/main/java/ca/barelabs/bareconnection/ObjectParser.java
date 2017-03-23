package ca.barelabs.bareconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface ObjectParser {

    <T> T parse(String value, Type type) throws IOException;

    <T> T parseAndClose(InputStream in, String charset, Type type) throws IOException;

    void saveAndClose(Object object, OutputStream out, String charset) throws IOException;
}
