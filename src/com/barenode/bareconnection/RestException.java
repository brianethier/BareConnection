package com.barenode.bareconnection;


@SuppressWarnings("serial")
public class RestException extends Exception {

    private final int mStatusCode;
    private final String mErrorResponse;


    public RestException(int statusCode, Throwable cause) {
        super(cause);
        mStatusCode = statusCode;
        mErrorResponse = null;
    }

    public RestException(int statusCode, String errorResponse) {
        super();
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