package ca.barelabs.bareconnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public interface ObjectParser {

    <T> T parse(String value, Class<T> clss) throws IOException;

    <T> T parseAndClose(InputStream in, String charset, Class<T> clss) throws IOException;

    <T> List<T> parseListAndClose(InputStream in, String charset, Class<T> clss) throws IOException;

    void saveAndClose(Object object, OutputStream out, String charset) throws IOException;
}
