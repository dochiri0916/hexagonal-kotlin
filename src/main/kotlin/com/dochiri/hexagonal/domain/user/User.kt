package com.dochiri.hexagonal.domain.user

import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId
import java.time.LocalDateTime

@ConsistentCopyVisibility
data class User private constructor(
    val id: UserId,
    val email: Email,
    val passwordHash: String,
    val name: String,
    val status: UserStatus,
    val role: UserRole,
    val lastLoginAt: LocalDateTime?
) {
    init {
        require(passwordHash.isNotBlank()) { "passwordHash는 비어 있을 수 없습니다." }
        require(name.isNotBlank()) { "name은 비어 있을 수 없습니다." }
    }

    fun updateLastLoginAt(now: LocalDateTime = LocalDateTime.now()): User =
        copy(lastLoginAt = now)

    companion object {
        fun register(
            email: Email,
            passwordHash: String,
            name: String
        ): User =
            User(
                id = UserId.newId(),
                email = email,
                passwordHash = passwordHash,
                name = name.trim(),
                status = UserStatus.ACTIVE,
                role = UserRole.USER,
                lastLoginAt = null
            )

        fun reconstitute(
            id: UserId,
            email: Email,
            passwordHash: String,
            name: String,
            status: UserStatus,
            role: UserRole,
            lastLoginAt: LocalDateTime?
        ): User =
            User(
                id = id,
                email = email,
                passwordHash = passwordHash,
                name = name,
                status = status,
                role = role,
                lastLoginAt = lastLoginAt
            )
    }
}
