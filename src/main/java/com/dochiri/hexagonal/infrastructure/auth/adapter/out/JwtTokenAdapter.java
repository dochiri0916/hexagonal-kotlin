package com.dochiri.hexagonal.infrastructure.auth.adapter.out;

import com.dochiri.hexagonal.application.user.port.out.JwtTokenPort;
import com.dochiri.hexagonal.infrastructure.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenAdapter implements JwtTokenPort {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_CATEGORY = "category";
    private static final String CATEGORY_ACCESS = "access";
    private static final String CATEGORY_REFRESH = "refresh";

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(String userPublicId, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userPublicId)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_CATEGORY, CATEGORY_ACCESS)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.accessExpiration()))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(String userPublicId, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(userPublicId)
                .claim(CLAIM_ROLE, role)
                .claim(CLAIM_CATEGORY, CATEGORY_REFRESH)
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtProperties.refreshExpiration()))
                .signWith(signingKey())
                .compact();
    }

    @Override
    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public String extractUserPublicId(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    @Override
    public boolean isAccessToken(String token) {
        return CATEGORY_ACCESS.equals(parseClaims(token).get(CLAIM_CATEGORY, String.class));
    }

    @Override
    public boolean isRefreshToken(String token) {
        return CATEGORY_REFRESH.equals(parseClaims(token).get(CLAIM_CATEGORY, String.class));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}