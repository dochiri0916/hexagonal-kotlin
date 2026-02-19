package com.dochiri.hexagonal.presentation.auth

import com.dochiri.hexagonal.application.user.dto.LoginUserCommand
import com.dochiri.hexagonal.application.user.dto.RegisterUserCommand
import com.dochiri.hexagonal.application.user.port.`in`.LoginUserUseCase
import com.dochiri.hexagonal.application.user.port.`in`.RegisterUserUseCase
import com.dochiri.hexagonal.presentation.auth.request.LoginUserRequest
import com.dochiri.hexagonal.presentation.auth.request.RegisterUserRequest
import com.dochiri.hexagonal.presentation.auth.response.LoginUserResponse
import com.dochiri.hexagonal.presentation.auth.response.RegisterUserResponse
import com.dochiri.hexagonal.presentation.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUserUseCase: LoginUserUseCase
) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerUserRequest: RegisterUserRequest): ApiResponse<RegisterUserResponse> {
        val result = registerUserUseCase.register(
            RegisterUserCommand(
                registerUserRequest.email,
                registerUserRequest.password,
                registerUserRequest.name
            )
        )
        return ApiResponse.success(RegisterUserResponse.from(result))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginUserRequest: LoginUserRequest): ApiResponse<LoginUserResponse> {
        val result = loginUserUseCase.login(
            LoginUserCommand(
                loginUserRequest.email,
                loginUserRequest.password
            )
        )
        return ApiResponse.success(LoginUserResponse.from(result))
    }
}
