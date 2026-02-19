package com.dochiri.hexagonal.infrastructure.auth.adapter.out

import com.dochiri.hexagonal.application.user.port.out.JwtTokenExtractor
import com.dochiri.hexagonal.application.user.port.out.JwtTokenGenerator
import com.dochiri.hexagonal.application.user.port.out.JwtTokenValidator
import com.dochiri.hexagonal.infrastructure.auth.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenAdapter(
    private val jwtProperties: JwtProperties
) : JwtTokenGenerator, JwtTokenValidator, JwtTokenExtractor {

    override fun generateAccessToken(userPublicId: String, role: String): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(userPublicId)
            .claim(CLAIM_ROLE, role)
            .claim(CLAIM_CATEGORY, CATEGORY_ACCESS)
            .issuedAt(Date(now))
            .expiration(Date(now + jwtProperties.accessExpiration))
            .signWith(signingKey())
            .compact()
    }

    override fun generateRefreshToken(userPublicId: String, role: String): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(userPublicId)
            .claim(CLAIM_ROLE, role)
            .claim(CLAIM_CATEGORY, CATEGORY_REFRESH)
            .issuedAt(Date(now))
            .expiration(Date(now + jwtProperties.refreshExpiration))
            .signWith(signingKey())
            .compact()
    }

    override fun validate(token: String): Boolean =
        try {
            Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
            true
        } catch (_: Exception) {
            false
        }

    override fun isExpired(token: String): Boolean =
        try {
            parseClaims(token).expiration.before(Date())
        } catch (_: Exception) {
            true
        }

    override fun extractUserPublicId(token: String): String = parseClaims(token).subject

    override fun extractRole(token: String): String = parseClaims(token).get(CLAIM_ROLE, String::class.java)

    override fun isAccessToken(token: String): Boolean =
        CATEGORY_ACCESS == parseClaims(token).get(CLAIM_CATEGORY, String::class.java)

    override fun isRefreshToken(token: String): Boolean =
        CATEGORY_REFRESH == parseClaims(token).get(CLAIM_CATEGORY, String::class.java)

    private fun signingKey(): SecretKey =
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray(StandardCharsets.UTF_8))

    private fun parseClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .payload

    companion object {
        private const val CLAIM_ROLE = "role"
        private const val CLAIM_CATEGORY = "category"
        private const val CATEGORY_ACCESS = "access"
        private const val CATEGORY_REFRESH = "refresh"
    }
}
