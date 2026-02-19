package com.dochiri.hexagonal.application.user.service.command

import com.dochiri.hexagonal.application.user.dto.LoginUserCommand
import com.dochiri.hexagonal.application.user.port.out.JwtTokenGenerator
import com.dochiri.hexagonal.application.user.port.out.LoadUserPort
import com.dochiri.hexagonal.application.user.port.out.PasswordHashPort
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort
import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.domain.user.UserRole
import com.dochiri.hexagonal.domain.user.UserStatus
import com.dochiri.hexagonal.domain.user.exception.InactiveUserException
import com.dochiri.hexagonal.domain.user.exception.InvalidPasswordException
import com.dochiri.hexagonal.domain.user.exception.UserNotFoundException
import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class LoginUserServiceTest {

    private val loadUserPort = mockk<LoadUserPort>()
    private val saveUserPort = mockk<SaveUserPort>()
    private val passwordHashPort = mockk<PasswordHashPort>()
    private val jwtTokenGenerator = mockk<JwtTokenGenerator>()

    private val service = LoginUserService(loadUserPort, saveUserPort, passwordHashPort, jwtTokenGenerator)

    @Test
    fun `login throws when user does not exist`() {
        every { loadUserPort.findByEmail(any()) } returns null

        assertThrows(UserNotFoundException::class.java) {
            service.login(LoginUserCommand("missing@example.com", "password123"))
        }
    }

    @Test
    fun `login throws when password does not match`() {
        val user = activeUser(passwordHash = "encoded")
        every { loadUserPort.findByEmail(any()) } returns user
        every { passwordHashPort.matches("wrong-password", "encoded") } returns false

        assertThrows(InvalidPasswordException::class.java) {
            service.login(LoginUserCommand("user@example.com", "wrong-password"))
        }
    }

    @Test
    fun `login throws when user is inactive`() {
        val inactiveUser = activeUser(status = UserStatus.INACTIVE)
        every { loadUserPort.findByEmail(any()) } returns inactiveUser
        every { passwordHashPort.matches(any(), any()) } returns true

        assertThrows(InactiveUserException::class.java) {
            service.login(LoginUserCommand("user@example.com", "password123"))
        }
    }

    @Test
    fun `login returns token for valid user`() {
        val user = activeUser(passwordHash = "encoded")
        every { loadUserPort.findByEmail(any()) } returns user
        every { passwordHashPort.matches("password123", "encoded") } returns true
        every { saveUserPort.save(any()) } answers { firstArg() }
        every { jwtTokenGenerator.generateAccessToken(any(), any()) } returns "access-token"

        val result = service.login(LoginUserCommand("user@example.com", "password123"))

        assertEquals("user@example.com", result.email)
        assertEquals("access-token", result.accessToken)
        verify(exactly = 1) { saveUserPort.save(any()) }
        verify(exactly = 1) { jwtTokenGenerator.generateAccessToken(any(), any()) }
    }

    private fun activeUser(
        status: UserStatus = UserStatus.ACTIVE,
        passwordHash: String = "encoded"
    ): User = User.reconstitute(
        id = UserId.from("user-123"),
        email = Email.from("user@example.com"),
        passwordHash = passwordHash,
        name = "User",
        status = status,
        role = UserRole.USER,
        lastLoginAt = null
    )
}
