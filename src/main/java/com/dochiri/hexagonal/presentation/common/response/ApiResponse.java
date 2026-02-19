package com.dochiri.hexagonal.presentation.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                LocalDateTime.now()
        );
    }

}