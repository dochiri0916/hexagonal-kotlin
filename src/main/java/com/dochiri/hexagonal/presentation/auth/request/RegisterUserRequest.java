package com.dochiri.hexagonal.presentation.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RegisterUserRequest(
        @NotNull
        @Email
        String email,

        @NotNull
        String password,

        @NotNull
        String name
) {
}