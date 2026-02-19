package com.dochiri.hexagonal.presentation.auth.response

import com.dochiri.hexagonal.application.user.dto.UserProfileResult

data class UserProfileResponse(
    val userId: String,
    val email: String,
    val name: String,
    val role: String
) {
    companion object {
        fun from(userProfileResult: UserProfileResult): UserProfileResponse =
            UserProfileResponse(
                userProfileResult.userId,
                userProfileResult.email,
                userProfileResult.name,
                userProfileResult.role
            )
    }
}
