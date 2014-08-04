package com.barenode.bareconnection;


@SuppressWarnings("serial")
public class RestException extends Exception {

    public static final int UNKNOWN_ERROR = -1;


    private int mResponseCode = UNKNOWN_ERROR;


    public RestException(int responseCode) {
        super();
        mResponseCode = responseCode;
    }

    public RestException(int responseCode, String message) {
        super(message);
        mResponseCode = responseCode;
    }

    public RestException(String message) {
        super(message);
    }

    public RestException(int responseCode, Throwable cause) {
        super(cause);
        mResponseCode = responseCode;
    }

    public RestException(Throwable cause) {
        super(cause);
    }

    public int getResponseCode() {
        return mResponseCode;
    }
}