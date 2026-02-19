package com.dochiri.hexagonal.application.user.port.out

interface JwtTokenValidator {
    fun validate(token: String): Boolean
    fun isExpired(token: String): Boolean
    fun isAccessToken(token: String): Boolean
    fun isRefreshToken(token: String): Boolean
}
