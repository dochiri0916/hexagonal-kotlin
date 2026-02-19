package com.dochiri.hexagonal.application.user.port.out

interface JwtTokenGenerator {
    fun generateAccessToken(userPublicId: String, role: String): String
    fun generateRefreshToken(userPublicId: String, role: String): String
}
