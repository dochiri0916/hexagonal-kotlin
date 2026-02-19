package com.dochiri.hexagonal.infrastructure.auth.adapter.`in`

import com.dochiri.hexagonal.application.user.port.out.JwtTokenExtractor
import com.dochiri.hexagonal.application.user.port.out.JwtTokenValidator
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtTokenValidator: JwtTokenValidator,
    private val jwtTokenExtractor: JwtTokenExtractor
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        resolveToken(request)
            ?.takeIf { jwtTokenValidator.validate(it) && jwtTokenValidator.isAccessToken(it) }
            ?.let { token ->
                val authentication = UsernamePasswordAuthenticationToken(
                    jwtTokenExtractor.extractUserPublicId(token),
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${jwtTokenExtractor.extractRole(token)}"))
                )
                SecurityContextHolder.getContext().authentication = authentication
            }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(httpServletRequest: HttpServletRequest): String? {
        val bearerToken = httpServletRequest.getHeader("Authorization")

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            return null
        }

        return bearerToken.substring(7)
    }
}
