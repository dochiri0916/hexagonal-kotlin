package com.dochiri.hexagonal.domain.user.vo;

import java.util.UUID;

import static java.util.Objects.*;

public record UserId(
        String value
) {
    public UserId {
        requireNonNull(value, "UserId는 null일 수 없습니다.");
        if (value.isBlank()) {
            throw new IllegalArgumentException("UserId는 비어있을 수 없습니다.");
        }
    }

    public static UserId from(String value) {
        return new UserId(value);
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID().toString());
    }

}