package com.dochiri.hexagonal.domain.user.vo

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class UserIdTest {

    @Test
    fun `from trims raw value`() {
        val userId = UserId.from("  user-123  ")

        assertEquals("user-123", userId.value)
    }

    @Test
    fun `from throws for blank value`() {
        assertThrows(IllegalArgumentException::class.java) {
            UserId.from("   ")
        }
    }
}
