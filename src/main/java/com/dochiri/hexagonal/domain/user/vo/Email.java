package com.dochiri.hexagonal.domain.user.vo;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public record Email(
        String value
) {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        requireNonNull(value, "Email은 null일 수 없습니다.");

        String trimmed = value.trim();

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다: " + trimmed);
        }

        value = trimmed.toLowerCase();
    }

    public static Email from(String rawEmail) {
        return new Email(rawEmail);
    }

}