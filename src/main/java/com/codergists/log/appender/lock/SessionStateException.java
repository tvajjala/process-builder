package com.codergists.log.appender.lock;

public class SessionStateException extends RuntimeException {

    public SessionStateException() {
    }

    public SessionStateException(String message) {
        super(message);
    }

    public SessionStateException(String message, Throwable cause) {
        super(message, cause);
    }

    public SessionStateException(Throwable cause) {
        super(cause);
    }

    public SessionStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
