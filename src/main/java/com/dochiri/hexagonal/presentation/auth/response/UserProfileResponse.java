package com.dochiri.hexagonal.presentation.auth.response;

import com.dochiri.hexagonal.application.user.dto.UserProfileResult;

public record UserProfileResponse(
        String userId,
        String email,
        String name,
        String role
) {
    public static UserProfileResponse from(UserProfileResult userProfileResult) {
        return new UserProfileResponse(
                userProfileResult.userId(),
                userProfileResult.email(),
                userProfileResult.name(),
                userProfileResult.role()
        );
    }
}