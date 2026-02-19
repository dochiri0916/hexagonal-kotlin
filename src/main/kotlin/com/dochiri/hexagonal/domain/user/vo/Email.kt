package com.dochiri.hexagonal.domain.user.vo

@JvmInline
value class Email private constructor(val value: String) {
    companion object {
        private val EMAIL_REGEX =
            Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun from(raw: String): Email {
            val normalized = raw.trim().lowercase()
            require(normalized.isNotBlank()) { "이메일은 비어 있을 수 없습니다." }
            require(EMAIL_REGEX.matches(normalized)) { "유효하지 않은 이메일 형식입니다: $normalized" }
            return Email(normalized)
        }
    }
}