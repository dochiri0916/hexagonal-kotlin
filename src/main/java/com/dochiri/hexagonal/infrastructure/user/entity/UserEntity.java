package com.dochiri.hexagonal.infrastructure.user.entity;

import com.dochiri.hexagonal.infrastructure.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 36)
    private String publicId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String role;

    private LocalDateTime lastLoginAt;

    public UserEntity(String publicId, String email, String passwordHash, String name, String status, String role, LocalDateTime lastLoginAt) {
        this.publicId = publicId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.status = status;
        this.role = role;
        this.lastLoginAt = lastLoginAt;
    }

    public void updateFromDomain(
            String email,
            String passwordHash,
            String name,
            String status,
            String role,
            LocalDateTime lastLoginAt
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.status = status;
        this.role = role;
        this.lastLoginAt = lastLoginAt;
    }

}
