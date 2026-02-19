package com.dochiri.hexagonal.domain.user.vo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class EmailTest {

    @Test
    fun `from normalizes and lowercases email`() {
        val email = Email.from("  Test.User@Example.COM  ")

        assertEquals("test.user@example.com", email.value)
    }

    @Test
    fun `from throws for invalid format`() {
        assertThrows(IllegalArgumentException::class.java) {
            Email.from("invalid-email")
        }
    }
}
