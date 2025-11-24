package com.soen343.tbd.application.exception;

public class StationFullException extends RuntimeException {
    public StationFullException() {
        super();
    }

    public StationFullException(String message) {
        super(message);
    }

    public StationFullException(String message, Throwable cause) {
        super(message, cause);
    }
}

