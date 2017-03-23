package ca.barelabs.bareconnection;

import java.io.IOException;


@SuppressWarnings("serial")
public class RestException extends IOException {

    private final int mStatusCode;
    private final String mErrorResponse;


    public RestException(int statusCode, Throwable cause) {
        super("(" + statusCode + ")", cause);
        mStatusCode = statusCode;
        mErrorResponse = null;
    }

    public RestException(int statusCode, String errorResponse) {
        super("(" + statusCode + ") " + errorResponse);
        mStatusCode = statusCode;
        mErrorResponse = errorResponse;
    }

    
    public int getStatusCode() {
        return mStatusCode;
    }

    public String getErrorResponse() {
        return mErrorResponse;
    }
    
    public boolean isStatusCodeKnown() {
        return mStatusCode != RestConnection.SC_UNKNOWN;
    }
    
    public boolean isStatusCodeSuccessful() {
        return mStatusCode / 100 == 2;
    }
    
    public boolean isErrorResponse() {
        return mErrorResponse != null;
    }
}