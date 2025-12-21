package org.example.chain.global.error.data;

import org.springframework.http.HttpStatus;

public record ErrorRes(String message, HttpStatus httpStatus) {
}
