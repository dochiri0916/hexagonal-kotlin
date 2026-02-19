package com.dochiri.hexagonal.infrastructure.persistence

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

    @field:CreatedDate
    @field:Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime? = null
        protected set

    @field:LastModifiedDate
    var updatedAt: LocalDateTime? = null
        protected set

    @field:CreatedBy
    @field:Column(nullable = false)
    var createdBy: String? = null
        protected set

    @field:LastModifiedBy
    var updatedBy: String? = null
        protected set

    var deletedAt: LocalDateTime? = null
        protected set

    fun markAsDeleted() {
        this.deletedAt = LocalDateTime.now()
    }

    fun markAsNotDeleted() {
        this.deletedAt = null
    }
}