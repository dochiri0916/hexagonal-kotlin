package com.dochiri.hexagonal.application.user.dto

data class RegisterUserResult(
    val id: String,
    val email: String
) {
    companion object {
        fun of(id: String, email: String): RegisterUserResult = RegisterUserResult(id, email)
    }
}
