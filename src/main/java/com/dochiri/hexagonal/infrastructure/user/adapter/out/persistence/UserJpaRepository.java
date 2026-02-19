package com.dochiri.hexagonal.infrastructure.user.adapter.out.persistence;

import com.dochiri.hexagonal.infrastructure.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByPublicId(String publicId);

    Optional<UserEntity> findByEmail(String email);

}