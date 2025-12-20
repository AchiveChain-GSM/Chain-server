package org.example.chain.global.error.exception;

import org.springframework.http.HttpStatus;

public class PostImageUploadFailedException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.NOT_FOUND;

    public PostImageUploadFailedException(String message) {
        super(message);
    }
}
