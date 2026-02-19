package com.dochiri.hexagonal.infrastructure.security.util;

import com.dochiri.hexagonal.infrastructure.security.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    private final CookieProperties cookieProperties;

    public void addAccessToken(HttpServletResponse response, String token) {
        addCookie(response, cookieProperties.accessTokenName(), token);
    }

    public void addRefreshToken(HttpServletResponse response, String token) {
        addCookie(response, cookieProperties.refreshTokenName(), token);
    }

    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath(cookieProperties.path());
        cookie.setMaxAge((int) cookieProperties.maxAge());
        cookie.setDomain(cookieProperties.domain());
        cookie.setHttpOnly(cookieProperties.httpOnly());
        cookie.setSecure(cookieProperties.secure());

        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(name).append("=").append(value).append("; ");
        cookieHeader.append("Path=").append(cookieProperties.path()).append("; ");
        cookieHeader.append("Max-Age=").append(cookieProperties.maxAge()).append("; ");
        cookieHeader.append("Domain=").append(cookieProperties.domain()).append("; ");

        if (cookieProperties.httpOnly()) {
            cookieHeader.append("HttpOnly; ");
        }

        if (cookieProperties.secure()) {
            cookieHeader.append("Secure; ");
        }

        if (cookieProperties.sameSite() != null) {
            cookieHeader.append("SameSite=").append(cookieProperties.sameSite()).append("; ");
        }

        response.addHeader("Set-Cookie", cookieHeader.toString());
    }

    public void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath(cookieProperties.path());
        cookie.setDomain(cookieProperties.domain());
        response.addCookie(cookie);
    }

}