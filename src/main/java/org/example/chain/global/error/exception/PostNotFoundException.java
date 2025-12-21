package org.example.chain.global.error.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PostNotFoundException extends RuntimeException {
    private final HttpStatus httpStatus = HttpStatus.NOT_FOUND;

    public PostNotFoundException() {
        super("게시글을 찾을 수 없습니다.");
    }

    public PostNotFoundException(String message) {
        super(message);
    }
}