package com.dochiri.hexagonal.infrastructure.persistence

import org.springframework.data.domain.AuditorAware
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class AuditorAwareImpl : AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        val authentication = SecurityContextHolder.getContext().authentication ?: return Optional.of("SYSTEM")

        if (authentication is AnonymousAuthenticationToken) {
            return Optional.of("SYSTEM")
        }

        if (!authentication.isAuthenticated) {
            return Optional.of("SYSTEM")
        }

        val principal = authentication.principal
        if (principal is String) {
            return Optional.of(principal)
        }

        return Optional.of("SYSTEM")
    }
}
