package com.ecommerce.backend.config;

import com.ecommerce.backend.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String header = httpRequest.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            if (JwtUtil.validateToken(token)) {

                String email = JwtUtil.extractEmail(token);
                String role = JwtUtil.extractRole(token);

                // 🔥 IMPORTANT FIX
                if (role == null || role.isEmpty()) {
                    throw new RuntimeException("Role missing in token");
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority(role)) // ✅ FIXED
                        );

                SecurityContextHolder.getContext().setAuthentication(auth);

                System.out.println("Authenticated user: " + email + " Role: " + role);
            }
        }

        chain.doFilter(request, response);
    }
}