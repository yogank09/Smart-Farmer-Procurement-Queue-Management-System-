package com.farmerprocurement.backend.config;

import com.farmerprocurement.backend.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider)

                .authorizeHttpRequests(auth -> auth

                        // Public authentication APIs
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/verify-email",
                                "/api/auth/login")
                        .permitAll()

                        .requestMatchers("/", "/error")
                        .permitAll()

                        // View and search procurement centers
                        .requestMatchers(HttpMethod.GET, "/api/centers/**")
                        .hasAnyRole("FARMER", "CENTER_OPERATOR", "ADMIN")

                        // Create procurement centers
                        .requestMatchers(HttpMethod.POST, "/api/centers/**")
                        .hasRole("ADMIN")

                        // Update procurement centers
                        .requestMatchers(HttpMethod.PUT, "/api/centers/**")
                        .hasRole("ADMIN")

                        // Activate/deactivate procurement centers
                        .requestMatchers(HttpMethod.PATCH, "/api/centers/**")
                        .hasRole("ADMIN")

                        // View procurement slots
                        .requestMatchers(HttpMethod.GET, "/api/slots/**")
                        .hasAnyRole("FARMER", "CENTER_OPERATOR", "ADMIN")

                        // Create procurement slots
                        .requestMatchers(HttpMethod.POST, "/api/slots/**")
                        .hasRole("ADMIN")

                        // Update procurement slots
                        .requestMatchers(HttpMethod.PUT, "/api/slots/**")
                        .hasRole("ADMIN")

                        // Modify procurement slot status
                        .requestMatchers(HttpMethod.PATCH, "/api/slots/**")
                        .hasRole("ADMIN")

                        // Delete procurement slots
                        .requestMatchers(HttpMethod.DELETE, "/api/slots/**")
                        .hasRole("ADMIN")

                        // All other APIs require authentication
                        .anyRequest()
                        .authenticated())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}