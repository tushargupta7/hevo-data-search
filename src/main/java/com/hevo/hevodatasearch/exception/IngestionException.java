package com.hevo.hevodatasearch.exception;

public class IngestionException extends RuntimeException {

    public IngestionException(String message, Exception cause) {
        super(message, cause);
    }

}