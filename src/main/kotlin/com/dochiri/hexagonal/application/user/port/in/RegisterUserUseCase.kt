package com.dochiri.hexagonal.application.user.port.`in`

import com.dochiri.hexagonal.application.user.dto.RegisterUserCommand
import com.dochiri.hexagonal.application.user.dto.RegisterUserResult

interface RegisterUserUseCase {
    fun register(registerUserCommand: RegisterUserCommand): RegisterUserResult
}
