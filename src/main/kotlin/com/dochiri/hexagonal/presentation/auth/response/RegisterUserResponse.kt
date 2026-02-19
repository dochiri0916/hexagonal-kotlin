package com.dochiri.hexagonal.presentation.auth.response

import com.dochiri.hexagonal.application.user.dto.RegisterUserResult

data class RegisterUserResponse(
    val id: String,
    val email: String
) {
    companion object {
        fun from(result: RegisterUserResult): RegisterUserResponse =
            RegisterUserResponse(result.id, result.email)
    }
}
