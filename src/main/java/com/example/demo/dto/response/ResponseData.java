package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseData<T> {
    private final int status;
    private final String message;
    private T data;

    public ResponseData(int status, String message) {
        this.status = status;
        this.message = message;
    }
}