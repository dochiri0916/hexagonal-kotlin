package com.dochiri.hexagonal.application.user.dto;

public record LoginUserCommand(
        String email,
        String password
) {
}