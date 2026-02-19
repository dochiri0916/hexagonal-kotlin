package com.dochiri.hexagonal.application.user.port.out

interface PasswordHashPort {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}
