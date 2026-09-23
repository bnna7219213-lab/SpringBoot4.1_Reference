package com.example.jpaadv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA Configuration:
 *
 * - @EnableJpaAuditing: enables automatic @CreatedDate/@LastModifiedDate population
 * - AuditorAware<String>: provides the current auditor (user) for audit fields
 *   In production this would come from Spring Security context
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        // Demo: return a fixed auditor. In production, extract from SecurityContext.
        return () -> Optional.of("system-admin");
    }
}
