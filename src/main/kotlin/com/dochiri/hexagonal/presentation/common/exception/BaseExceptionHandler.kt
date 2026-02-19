package com.dochiri.hexagonal.presentation.common.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import java.time.LocalDateTime

open class BaseExceptionHandler {

    protected fun handle(status: HttpStatus, exception: Exception, request: HttpServletRequest): ProblemDetail {
        logException(status, exception, request)
        return createProblemDetail(status, exception, request)
    }

    private fun logException(status: HttpStatus, exception: Exception, request: HttpServletRequest) {
        if (status.is5xxServerError) {
            logger.error(
                "[{}]: uri={}, method={}, message={}",
                exception::class.java.simpleName,
                request.requestURI,
                request.method,
                exception.message,
                exception
            )
            return
        }

        logger.warn(
            "[{}] uri={}, method={}, message={}",
            exception::class.java.simpleName,
            request.requestURI,
            request.method,
            exception.message,
            exception
        )
    }

    private fun createProblemDetail(
        status: HttpStatus,
        exception: Exception,
        request: HttpServletRequest
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, exception.message)

        problemDetail.setProperty("timestamp", LocalDateTime.now())
        problemDetail.setProperty("exception", exception::class.java.simpleName)
        problemDetail.setProperty("path", request.requestURI)

        return problemDetail
    }

    companion object {
        private val logger = LoggerFactory.getLogger(BaseExceptionHandler::class.java)
    }
}
