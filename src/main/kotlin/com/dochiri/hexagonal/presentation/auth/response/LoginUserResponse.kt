package com.dochiri.hexagonal.presentation.auth.response

import com.dochiri.hexagonal.application.user.dto.LoginUserResult

data class LoginUserResponse(
    val id: String,
    val email: String,
    val role: String,
    val accessToken: String
) {
    companion object {
        fun from(result: LoginUserResult): LoginUserResponse =
            LoginUserResponse(
                result.id,
                result.email,
                result.role,
                result.accessToken
            )
    }
}
