package org.example.chain.global.error.exception;

import org.springframework.http.HttpStatus;

public class PostImageDeleteFailedException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

    public PostImageDeleteFailedException(String message) {
        super(message);
    }
}
