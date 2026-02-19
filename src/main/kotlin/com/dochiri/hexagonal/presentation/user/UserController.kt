package com.dochiri.hexagonal.presentation.user

import com.dochiri.hexagonal.application.user.port.`in`.GetUserProfileQuery
import com.dochiri.hexagonal.presentation.auth.response.UserProfileResponse
import com.dochiri.hexagonal.presentation.common.response.ApiResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val getUserProfileQuery: GetUserProfileQuery
) {

    @GetMapping("/me")
    fun getProfile(@AuthenticationPrincipal userPublicId: String): ApiResponse<UserProfileResponse> =
        ApiResponse.success(UserProfileResponse.from(getUserProfileQuery.getProfile(userPublicId)))
}
