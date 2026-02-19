package com.dochiri.hexagonal.domain.user.vo

import java.util.UUID

@JvmInline
value class UserId private constructor(val value: String) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID().toString())

        fun from(raw: String): UserId {
            val normalized = raw.trim()
            require(normalized.isNotBlank()) { "UserId는 비어 있을 수 없습니다." }
            return UserId(normalized)
        }

        fun newId(): UserId = generate()
    }
}