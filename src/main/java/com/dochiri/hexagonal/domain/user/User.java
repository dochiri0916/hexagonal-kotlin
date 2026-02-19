package com.dochiri.hexagonal.domain.user;

import com.dochiri.hexagonal.domain.user.vo.Email;
import com.dochiri.hexagonal.domain.user.vo.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

import static java.util.Objects.*;

@Getter
public class User {

    private final UserId id;
    private final Email email;
    private final String passwordHash;
    private final String name;
    private final UserStatus status;
    private final UserRole role;
    private final LocalDateTime lastLoginAt;

    private User(UserId id, Email email, String passwordHash, String name, UserStatus status, UserRole role, LocalDateTime lastLoginAt) {
        this.id = requireNonNull(id);
        this.email = requireNonNull(email);
        this.passwordHash = requireNonNull(passwordHash);
        this.name = requireNonNull(name);
        this.status = requireNonNull(status);
        this.role = requireNonNull(role);
        this.lastLoginAt = lastLoginAt;
    }

    public static User register(Email email, String passwordHash, String name) {
        return new User(
                UserId.newId(),
                email,
                passwordHash,
                name,
                UserStatus.ACTIVE,
                UserRole.USER,
                null
        );
    }

    public static User of(
            UserId id,
            Email email,
            String passwordHash,
            String name,
            UserStatus status,
            UserRole role,
            LocalDateTime lastLoginAt
    ) {
        return new User(
                id,
                email,
                passwordHash,
                name,
                status,
                role,
                lastLoginAt
        );
    }

    public User updateLastLoginAt() {
        return new User(
                id,
                email,
                passwordHash,
                name,
                status,
                role,
                LocalDateTime.now()
        );
    }

}
