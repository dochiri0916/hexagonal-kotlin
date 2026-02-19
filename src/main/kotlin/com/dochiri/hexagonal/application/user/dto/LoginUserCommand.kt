package com.dochiri.hexagonal.application.user.dto

data class LoginUserCommand(
    val email: String,
    val password: String
)
