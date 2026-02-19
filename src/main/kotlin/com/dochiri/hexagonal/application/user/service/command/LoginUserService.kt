package com.dochiri.hexagonal.application.user.service.command

import com.dochiri.hexagonal.application.user.dto.LoginUserCommand
import com.dochiri.hexagonal.application.user.dto.LoginUserResult
import com.dochiri.hexagonal.application.user.port.`in`.LoginUserUseCase
import com.dochiri.hexagonal.application.user.port.out.JwtTokenGenerator
import com.dochiri.hexagonal.application.user.port.out.LoadUserPort
import com.dochiri.hexagonal.application.user.port.out.PasswordHashPort
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort
import com.dochiri.hexagonal.domain.user.UserStatus
import com.dochiri.hexagonal.domain.user.exception.InactiveUserException
import com.dochiri.hexagonal.domain.user.exception.InvalidPasswordException
import com.dochiri.hexagonal.domain.user.exception.UserNotFoundException
import com.dochiri.hexagonal.domain.user.vo.Email
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginUserService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val passwordHashPort: PasswordHashPort,
    private val jwtTokenGenerator: JwtTokenGenerator
) : LoginUserUseCase {

    @Transactional
    override fun login(loginUserCommand: LoginUserCommand): LoginUserResult {
        val email = Email.from(loginUserCommand.email)
        val user = loadUserPort.findByEmail(email) ?: throw UserNotFoundException.byEmail(email)

        if (!passwordHashPort.matches(loginUserCommand.password, user.passwordHash)) {
            throw InvalidPasswordException()
        }

        if (user.status != UserStatus.ACTIVE) {
            throw InactiveUserException()
        }

        val updatedUser = saveUserPort.save(user.updateLastLoginAt())

        val accessToken = jwtTokenGenerator.generateAccessToken(
            updatedUser.id.value,
            updatedUser.role.name
        )

        return LoginUserResult.of(
            updatedUser.id.value,
            updatedUser.email.value,
            updatedUser.role.name,
            accessToken
        )
    }
}
