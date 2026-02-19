package com.dochiri.hexagonal.infrastructure.auth.adapter.in;

import com.dochiri.hexagonal.application.user.port.out.JwtTokenPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenPort jwtTokenPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtTokenPort.validate(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!jwtTokenPort.isAccessToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String userPublicId = jwtTokenPort.extractUserPublicId(token);
        String role = jwtTokenPort.extractRole(token);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userPublicId,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader("Authorization");

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return null;
        }

        return bearerToken.substring(7);
    }

}
