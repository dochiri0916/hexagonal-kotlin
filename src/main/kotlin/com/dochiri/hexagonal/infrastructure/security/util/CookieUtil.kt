package com.dochiri.hexagonal.infrastructure.security.util

import com.dochiri.hexagonal.infrastructure.security.CookieProperties
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class CookieUtil(
    private val cookieProperties: CookieProperties
) {

    fun addAccessToken(response: HttpServletResponse, token: String) {
        addCookie(response, cookieProperties.accessTokenName, token)
    }

    fun addRefreshToken(response: HttpServletResponse, token: String) {
        addCookie(response, cookieProperties.refreshTokenName, token)
    }

    private fun addCookie(response: HttpServletResponse, name: String, value: String) {
        val cookie = Cookie(name, value)
        cookie.path = cookieProperties.path
        cookie.maxAge = cookieProperties.maxAge.toInt()
        cookie.domain = cookieProperties.domain
        cookie.isHttpOnly = cookieProperties.httpOnly
        cookie.secure = cookieProperties.secure

        val cookieHeader = StringBuilder()
        cookieHeader.append(name).append("=").append(value).append("; ")
        cookieHeader.append("Path=").append(cookieProperties.path).append("; ")
        cookieHeader.append("Max-Age=").append(cookieProperties.maxAge).append("; ")
        cookieHeader.append("Domain=").append(cookieProperties.domain).append("; ")

        if (cookieProperties.httpOnly) {
            cookieHeader.append("HttpOnly; ")
        }

        if (cookieProperties.secure) {
            cookieHeader.append("Secure; ")
        }

        if (cookieProperties.sameSite.isNotEmpty()) {
            cookieHeader.append("SameSite=").append(cookieProperties.sameSite).append("; ")
        }

        response.addHeader("Set-Cookie", cookieHeader.toString())
    }

    fun deleteCookie(response: HttpServletResponse, name: String) {
        val cookie = Cookie(name, null)
        cookie.maxAge = 0
        cookie.path = cookieProperties.path
        cookie.domain = cookieProperties.domain
        response.addCookie(cookie)
    }
}
