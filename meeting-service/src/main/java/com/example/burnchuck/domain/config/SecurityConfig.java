package com.example.burnchuck.domain.config;

import com.example.burnchuck.common.config.BaseSecurityConfig;
import com.example.burnchuck.common.filter.JwtFilter;
import com.example.burnchuck.common.filter.SseAuthenticationFilter;
import com.example.burnchuck.common.jwt.JwtAccessDeniedHandler;
import com.example.burnchuck.common.jwt.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig extends BaseSecurityConfig {

    public SecurityConfig(JwtFilter jwtFilter, SseAuthenticationFilter sseAuthenticationFilter,
        JwtAuthenticationEntryPoint authenticationEntryPoint,
        JwtAccessDeniedHandler accessDeniedHandler) {
        super(jwtFilter, sseAuthenticationFilter, authenticationEntryPoint, accessDeniedHandler);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return applyCommonSecurity(http)
            .authorizeHttpRequests(auth -> applyCommonPermit(auth)
                .requestMatchers("/api/meetings/hosted-meetings").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/meetings/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                .anyRequest().authenticated()
            )
            .build();
    }
}
