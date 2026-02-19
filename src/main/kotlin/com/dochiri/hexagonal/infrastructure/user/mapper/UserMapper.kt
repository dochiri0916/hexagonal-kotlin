package com.dochiri.hexagonal.infrastructure.user.mapper

import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.domain.user.UserRole
import com.dochiri.hexagonal.domain.user.UserStatus
import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId
import com.dochiri.hexagonal.infrastructure.user.entity.UserEntity

fun User.toEntity(): UserEntity =
    UserEntity(
        publicId = id.value,
        email = email.value,
        passwordHash = passwordHash,
        name = name,
        status = status.name,
        role = role.name,
        lastLoginAt = lastLoginAt
    )

fun UserEntity.toDomain(): User =
    User.reconstitute(
        UserId.from(publicId),
        Email.from(email),
        passwordHash,
        name,
        UserStatus.valueOf(status),
        UserRole.valueOf(role),
        lastLoginAt
    )

fun UserEntity.applyFullUpdate(domain: User) {
    updateFromDomain(
        email = domain.email.value,
        passwordHash = domain.passwordHash,
        name = domain.name,
        status = domain.status.name,
        role = domain.role.name,
        lastLoginAt = domain.lastLoginAt
    )
}
