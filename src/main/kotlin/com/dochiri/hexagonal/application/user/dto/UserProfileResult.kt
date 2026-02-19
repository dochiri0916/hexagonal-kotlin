package com.dochiri.hexagonal.application.user.dto

import com.dochiri.hexagonal.domain.user.User

data class UserProfileResult(
    val userId: String,
    val email: String,
    val name: String,
    val role: String
) {
    companion object {
        fun from(user: User): UserProfileResult =
            UserProfileResult(
                user.id.value,
                user.email.value,
                user.name,
                user.role.name
            )
    }
}
