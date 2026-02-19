package com.dochiri.hexagonal.infrastructure.common.persistence;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return Optional.of("SYSTEM");
        }

        if (authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("SYSTEM");
        }

        if (!authentication.isAuthenticated()) {
            return Optional.of("SYSTEM");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof String publicId) {
            return Optional.of(publicId);
        }

        return Optional.of("SYSTEM");
    }

}
