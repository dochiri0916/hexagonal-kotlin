package com.dochiri.hexagonal.application.user.dto;

import com.dochiri.hexagonal.domain.user.User;

public record UserProfileResult(
        String userId,
        String email,
        String name,
        String role
) {
    public static UserProfileResult from(User user) {
        return new UserProfileResult(
                user.getId().value(),
                user.getEmail().value(),
                user.getName(),
                user.getRole().name()
        );
    }
}