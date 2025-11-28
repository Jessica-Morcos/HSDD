package org.hsdd.config;

import org.hsdd.security.TokenAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenAuthFilter tokenFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // PUBLIC
                        .requestMatchers("/api/auth/**").permitAll()

                        // ACCOUNT routes (authenticated users only)
                        .requestMatchers("/api/account/**").authenticated()

                        // ADMIN
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // DOCTOR
                        .requestMatchers("/api/doctor/**")
                        .hasAnyAuthority("ROLE_DOCTOR", "ROLE_ADMIN")

                        // PATIENT
                        .requestMatchers("/api/patient/**")
                        .hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR", "ROLE_ADMIN")

                        // HISTORY
                        .requestMatchers("/api/history/**")
                        .hasAnyAuthority("ROLE_PATIENT", "ROLE_DOCTOR", "ROLE_ADMIN")

                        // EVERYTHING ELSE
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
