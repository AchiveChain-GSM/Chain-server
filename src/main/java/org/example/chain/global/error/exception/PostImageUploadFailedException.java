package org.example.chain.global.error.exception;

import org.springframework.http.HttpStatus;

public class PostImageUploadFailedException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public PostImageUploadFailedException(String message) {
        super(message);
    }
}
