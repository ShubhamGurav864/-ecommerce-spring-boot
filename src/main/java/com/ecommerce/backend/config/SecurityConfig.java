package com.ecommerce.backend.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // 1. Swagger & Documentation - PUBLIC
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // 2. Authentication & Webhooks - PUBLIC
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/payment/webhook/stripe").permitAll()

                // 3. Admin Product Management - ADMIN ONLY
                // Note: hasRole("ADMIN") looks for "ROLE_ADMIN" authority
                .requestMatchers("/api/products/admin/**").hasRole("ADMIN")

                // 4. Browsing Products - PUBLIC (Only GET requests)
                // This must come AFTER the admin path to avoid intercepting it
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // 5. All other requests (e.g., /api/orders, /api/user/profile)
                .anyRequest().authenticated()
            )
            // Add your JWT filter before the standard authentication filter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}