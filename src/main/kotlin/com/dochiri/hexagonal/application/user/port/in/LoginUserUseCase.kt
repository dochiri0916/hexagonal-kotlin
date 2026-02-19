package com.dochiri.hexagonal.application.user.port.`in`

import com.dochiri.hexagonal.application.user.dto.LoginUserCommand
import com.dochiri.hexagonal.application.user.dto.LoginUserResult

interface LoginUserUseCase {
    fun login(loginUserCommand: LoginUserCommand): LoginUserResult
}
