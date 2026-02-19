package com.dochiri.hexagonal.application.user.port.out

interface JwtTokenExtractor {
    fun extractUserPublicId(token: String): String
    fun extractRole(token: String): String
}
