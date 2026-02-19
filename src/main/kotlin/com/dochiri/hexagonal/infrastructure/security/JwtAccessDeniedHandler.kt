package com.dochiri.hexagonal.infrastructure.security

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class JwtAccessDeniedHandler : AccessDeniedHandler {

    @Throws(IOException::class, ServletException::class)
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.characterEncoding = "UTF-8"
        response.contentType = "application/json"

        response.writer.write(
            """
            {
                "code": 403,
                "message": "FORBIDDEN"
            }
            """.trimIndent()
        )
    }
}
