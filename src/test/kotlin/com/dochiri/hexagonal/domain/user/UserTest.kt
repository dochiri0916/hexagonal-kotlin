package com.dochiri.hexagonal.domain.user

import com.dochiri.hexagonal.domain.user.vo.Email
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class UserTest {

    @Test
    fun `register creates active user with default role`() {
        val user = User.register(
            email = Email.from("user@example.com"),
            passwordHash = "hashed-password",
            name = "User"
        )

        assertEquals(UserStatus.ACTIVE, user.status)
        assertEquals(UserRole.USER, user.role)
        assertNull(user.lastLoginAt)
        assertNotNull(user.id.value)
    }

    @Test
    fun `updateLastLoginAt updates login time`() {
        val now = LocalDateTime.of(2026, 2, 19, 12, 0)
        val user = User.register(
            email = Email.from("user@example.com"),
            passwordHash = "hashed-password",
            name = "User"
        )

        val updated = user.updateLastLoginAt(now)

        assertEquals(now, updated.lastLoginAt)
        assertEquals(user.id, updated.id)
        assertEquals(user.email, updated.email)
    }
}
