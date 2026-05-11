package com.xapps.question_generator.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }                   // disable CSRF for POST/PUT without token
            .authorizeExchange { exchanges ->
                exchanges.anyExchange().permitAll()  // allow all requests
            }
            .httpBasic { it.disable() }              // disable basic auth
            .formLogin { it.disable() }              // disable form login
            .build()
    }
}
