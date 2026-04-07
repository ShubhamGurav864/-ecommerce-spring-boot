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

                // Swagger - PUBLIC
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Public: login and register
                .requestMatchers("/auth/**").permitAll()

                .requestMatchers("/api/payment/webhook/stripe").permitAll()

                // Public: browsing products (GET only)
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                // Admin only: create, update, delete products
                .requestMatchers("/api/products/admin/**").hasAuthority("ROLE_ADMIN")

                // Everything else: any valid token (user or admin)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}