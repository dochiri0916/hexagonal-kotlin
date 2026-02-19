package com.dochiri.hexagonal.application.user.port.`in`

import com.dochiri.hexagonal.application.user.dto.UserProfileResult

interface GetUserProfileQuery {
    fun getProfile(userPublicId: String): UserProfileResult
}
