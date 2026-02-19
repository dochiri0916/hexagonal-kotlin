package com.dochiri.hexagonal.application.user.dto;

public record RegisterUserResult(
        String id,
        String email
) {
    public static RegisterUserResult of(String id, String email) {
        return new RegisterUserResult(id, email);
    }
}
