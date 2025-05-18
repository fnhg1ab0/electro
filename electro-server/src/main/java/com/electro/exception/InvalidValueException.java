package com.electro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidValueException extends RuntimeException {

    private String fieldName;
    private Object value;
    private String reason;

    public InvalidValueException(String fieldName, Object value, String reason) {
        super(String.format("Giá trị không hợp lệ cho trường '%s': '%s'. Lý do: %s", fieldName, value, reason));
        this.fieldName = fieldName;
        this.value = value;
        this.reason = reason;
    }

    public InvalidValueException(String fieldName, Object value) {
        super(String.format("Giá trị không hợp lệ cho trường '%s': '%s'", fieldName, value));
        this.fieldName = fieldName;
        this.value = value;
    }

    public InvalidValueException(String message) {
        super(message);
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getValue() {
        return value;
    }

    public String getReason() {
        return reason;
    }
}