package com.dochiri.hexagonal.presentation.common.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val timestamp: LocalDateTime
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(
                success = true,
                data = data,
                timestamp = LocalDateTime.now()
            )
    }
}
