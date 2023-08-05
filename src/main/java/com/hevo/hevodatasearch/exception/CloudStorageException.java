package com.hevo.hevodatasearch.exception;

public class CloudStorageException extends RuntimeException {

    public CloudStorageException(String message) {
        super(message);
    }

    public CloudStorageException(String message, Exception cause) {
        super(message, cause);
    }

}
