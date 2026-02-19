package com.dochiri.hexagonal.application.user.dto

data class RegisterUserCommand(
    val email: String,
    val password: String,
    val name: String
)
