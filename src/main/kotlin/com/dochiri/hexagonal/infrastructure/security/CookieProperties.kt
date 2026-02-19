package com.dochiri.hexagonal.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "cookie")
data class CookieProperties(
    val accessTokenName: String,
    val refreshTokenName: String,
    val domain: String,
    val path: String,
    val httpOnly: Boolean,
    val secure: Boolean,
    val sameSite: String,
    val maxAge: Long
)
