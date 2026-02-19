package com.dochiri.hexagonal.infrastructure.user.adapter.out.persistence

import com.dochiri.hexagonal.infrastructure.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserJpaRepository : JpaRepository<UserEntity, Long> {
    fun findByPublicId(publicId: String): Optional<UserEntity>
    fun findByEmail(email: String): Optional<UserEntity>
}