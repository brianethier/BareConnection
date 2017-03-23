package ca.barelabs.bareconnection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;


public interface HttpURLConnectionFactory {

    HttpURLConnection createHttpURLConnection(String method) throws MalformedURLException, UnsupportedEncodingException, IOException;
}
