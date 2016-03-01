package ca.barelabs.bareconnection;

import java.net.HttpURLConnection;


public class RestResponse {
    
    private final HttpURLConnection mConnection;
    private final int mResponseCode;

    public RestResponse(HttpURLConnection connection, int responseCode) {
        mConnection = connection;
        mResponseCode = responseCode;
    }
    
    public HttpURLConnection getConnection() {
        return mConnection;
    }
    
    public int getResponseCode() {
        return mResponseCode;
    }
}
