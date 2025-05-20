package com.electro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidValueException extends RuntimeException {

    public InvalidValueException(String fieldName, Object value, String reason) {
        super(String.format("Giá trị không hợp lệ cho trường '%s': '%s'. Lý do: %s", fieldName, value, reason));
    }

    public InvalidValueException(String fieldName, Object value) {
        super(String.format("Giá trị không hợp lệ cho trường '%s': '%s'", fieldName, value));
    }
}