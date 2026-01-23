package com.epam.finaltask.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private T results;
    private String statusCode;
    private String statusMessage;

    public ApiResponse() {
    }

    public ApiResponse(T results) {
        this.results = results;
    }
}