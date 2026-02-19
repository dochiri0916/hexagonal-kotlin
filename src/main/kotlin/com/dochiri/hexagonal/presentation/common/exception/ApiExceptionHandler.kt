package com.dochiri.hexagonal.presentation.common.exception

import com.dochiri.hexagonal.domain.user.exception.DuplicateEmailException
import com.dochiri.hexagonal.domain.user.exception.InactiveUserException
import com.dochiri.hexagonal.domain.user.exception.InvalidPasswordException
import com.dochiri.hexagonal.domain.user.exception.UserNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ApiExceptionHandler : BaseExceptionHandler() {

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(exception: UserNotFoundException, request: HttpServletRequest): ProblemDetail =
        handle(HttpStatus.NOT_FOUND, exception, request)

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmailException(exception: DuplicateEmailException, request: HttpServletRequest): ProblemDetail =
        handle(HttpStatus.CONFLICT, exception, request)

    @ExceptionHandler(InvalidPasswordException::class)
    fun handleInvalidPasswordException(exception: InvalidPasswordException, request: HttpServletRequest): ProblemDetail =
        handle(HttpStatus.BAD_REQUEST, exception, request)

    @ExceptionHandler(InactiveUserException::class)
    fun handleInactiveUserException(exception: InactiveUserException, request: HttpServletRequest): ProblemDetail =
        handle(HttpStatus.FORBIDDEN, exception, request)

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception, request: HttpServletRequest): ProblemDetail =
        handle(HttpStatus.INTERNAL_SERVER_ERROR, exception, request)
}
