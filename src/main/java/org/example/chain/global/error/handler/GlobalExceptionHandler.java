package org.example.chain.global.error.handler;

import org.example.chain.global.error.data.ErrorRes;
import org.example.chain.global.error.exception.PostNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorRes> handlerPostNotFoundException(PostNotFoundException e){
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(new ErrorRes(e.getMessage(), e.getHttpStatus()));
    }
}
