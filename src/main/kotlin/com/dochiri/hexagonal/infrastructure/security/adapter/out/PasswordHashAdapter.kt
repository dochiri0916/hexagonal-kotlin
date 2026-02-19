package com.dochiri.hexagonal.infrastructure.security.adapter.out

import com.dochiri.hexagonal.application.user.port.out.PasswordHashPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordHashAdapter(
    private val passwordEncoder: PasswordEncoder
) : PasswordHashPort {

    override fun encode(rawPassword: String): String =
        requireNotNull(passwordEncoder.encode(rawPassword))

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
