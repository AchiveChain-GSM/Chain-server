package org.example.chain.global.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PostNotFoundException extends RuntimeException {
    HttpStatus httpStatus;
    public PostNotFoundException(String message, HttpStatus httpStatus){
        super(message);
        this.httpStatus = httpStatus;
    }
}
