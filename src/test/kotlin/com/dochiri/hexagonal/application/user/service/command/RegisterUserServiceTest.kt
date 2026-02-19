package com.dochiri.hexagonal.application.user.service.command

import com.dochiri.hexagonal.application.user.dto.RegisterUserCommand
import com.dochiri.hexagonal.application.user.port.out.LoadUserPort
import com.dochiri.hexagonal.application.user.port.out.PasswordHashPort
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort
import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.domain.user.exception.DuplicateEmailException
import com.dochiri.hexagonal.domain.user.vo.Email
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RegisterUserServiceTest {

    private val loadUserPort = mockk<LoadUserPort>()
    private val saveUserPort = mockk<SaveUserPort>()
    private val passwordHashPort = mockk<PasswordHashPort>()

    private val service = RegisterUserService(loadUserPort, saveUserPort, passwordHashPort)

    @Test
    fun `register throws when email already exists`() {
        val existing = User.register(Email.from("exists@example.com"), "hash", "Existing")
        every { loadUserPort.findByEmail(any()) } returns existing

        assertThrows(DuplicateEmailException::class.java) {
            service.register(RegisterUserCommand("exists@example.com", "password123", "New User"))
        }

        verify(exactly = 0) { saveUserPort.save(any()) }
    }

    @Test
    fun `register saves and returns result for new user`() {
        every { loadUserPort.findByEmail(any()) } returns null
        every { passwordHashPort.encode("password123") } returns "encoded-password"
        every { saveUserPort.save(any()) } answers { firstArg() }

        val result = service.register(RegisterUserCommand("new@example.com", "password123", "New User"))

        assertEquals("new@example.com", result.email)
        verify(exactly = 1) { saveUserPort.save(any()) }
    }
}
