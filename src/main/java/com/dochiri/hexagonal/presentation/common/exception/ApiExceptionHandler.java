package com.dochiri.hexagonal.presentation.common.exception;

import com.dochiri.hexagonal.domain.user.exception.DuplicateEmailException;
import com.dochiri.hexagonal.domain.user.exception.InactiveUserException;
import com.dochiri.hexagonal.domain.user.exception.InvalidPasswordException;
import com.dochiri.hexagonal.domain.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException exception, HttpServletRequest request) {
        return handle(HttpStatus.NOT_FOUND, exception, request);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmailException(DuplicateEmailException exception, HttpServletRequest request) {
        return handle(HttpStatus.CONFLICT, exception, request);
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ProblemDetail handleInvalidPasswordException(InvalidPasswordException exception, HttpServletRequest request) {
        return handle(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(InactiveUserException.class)
    public ProblemDetail handleInactiveUserException(InactiveUserException exception, HttpServletRequest request) {
        return handle(HttpStatus.FORBIDDEN, exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception, HttpServletRequest request) {
        return handle(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
    }

}