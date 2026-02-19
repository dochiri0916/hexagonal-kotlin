package com.dochiri.hexagonal.application.user.service.command

import com.dochiri.hexagonal.application.user.dto.RegisterUserCommand
import com.dochiri.hexagonal.application.user.dto.RegisterUserResult
import com.dochiri.hexagonal.application.user.port.`in`.RegisterUserUseCase
import com.dochiri.hexagonal.application.user.port.out.LoadUserPort
import com.dochiri.hexagonal.application.user.port.out.PasswordHashPort
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort
import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.domain.user.exception.DuplicateEmailException
import com.dochiri.hexagonal.domain.user.vo.Email
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterUserService(
    private val loadUserPort: LoadUserPort,
    private val saveUserPort: SaveUserPort,
    private val passwordHashPort: PasswordHashPort
) : RegisterUserUseCase {

    @Transactional
    override fun register(registerUserCommand: RegisterUserCommand): RegisterUserResult {
        val email = Email.from(registerUserCommand.email)

        loadUserPort.findByEmail(email)?.let { throw DuplicateEmailException(email) }

        val passwordHash = passwordHashPort.encode(registerUserCommand.password)
        val newUser = User.register(email, passwordHash, registerUserCommand.name)
        val savedUser = saveUserPort.save(newUser)

        return RegisterUserResult.of(
            savedUser.id.value,
            savedUser.email.value
        )
    }
}
