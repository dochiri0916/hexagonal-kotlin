package com.dochiri.hexagonal.application.user.dto

data class LoginUserResult(
    val id: String,
    val email: String,
    val role: String,
    val accessToken: String
) {
    companion object {
        fun of(id: String, email: String, role: String, accessToken: String): LoginUserResult =
            LoginUserResult(id, email, role, accessToken)
    }
}
