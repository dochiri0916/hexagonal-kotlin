package com.dochiri.hexagonal.application.user.dto;

public record LoginUserResult(
        String id,
        String email,
        String role,
        String accessToken
) {
    public static LoginUserResult of(String id, String email, String role, String accessToken) {
        return new LoginUserResult(id, email, role, accessToken);
    }
}