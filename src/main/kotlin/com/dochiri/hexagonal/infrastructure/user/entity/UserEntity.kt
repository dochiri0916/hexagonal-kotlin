package com.dochiri.hexagonal.infrastructure.user.entity

import com.dochiri.hexagonal.infrastructure.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 36)
    val publicId: String = "",

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var passwordHash: String = "",

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var role: String = "",

    @Column(nullable = false)
    var status: String = "",
    var lastLoginAt: LocalDateTime? = null
) : BaseEntity() {

    fun updateFromDomain(
        email: String,
        passwordHash: String,
        name: String,
        status: String,
        role: String,
        lastLoginAt: LocalDateTime?
    ) {
        this.email = email
        this.passwordHash = passwordHash
        this.name = name
        this.status = status
        this.role = role
        this.lastLoginAt = lastLoginAt
    }
}
