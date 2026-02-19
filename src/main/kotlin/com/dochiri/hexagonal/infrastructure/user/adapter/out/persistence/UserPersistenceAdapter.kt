package com.dochiri.hexagonal.infrastructure.user.adapter.out.persistence

import com.dochiri.hexagonal.application.user.port.out.LoadUserPort
import com.dochiri.hexagonal.application.user.port.out.SaveUserPort
import com.dochiri.hexagonal.domain.user.User
import com.dochiri.hexagonal.domain.user.vo.Email
import com.dochiri.hexagonal.domain.user.vo.UserId
import com.dochiri.hexagonal.infrastructure.user.mapper.applyFullUpdate
import com.dochiri.hexagonal.infrastructure.user.mapper.toDomain
import com.dochiri.hexagonal.infrastructure.user.mapper.toEntity
import org.springframework.stereotype.Repository

@Repository
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository
) : LoadUserPort, SaveUserPort {

    override fun findById(userId: UserId): User? =
        userJpaRepository.findByPublicId(userId.value)
            .orElse(null)
            ?.toDomain()

    override fun findByEmail(email: Email): User? =
        userJpaRepository.findByEmail(email.value)
            .orElse(null)
            ?.toDomain()

    override fun save(user: User): User {
        val entity = userJpaRepository.findByPublicId(user.id.value)
            .orElse(null)
            ?.also { it.applyFullUpdate(user) }
            ?: user.toEntity()

        return userJpaRepository.save(entity).toDomain()
    }
}
