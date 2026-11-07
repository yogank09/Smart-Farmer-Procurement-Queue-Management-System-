package com.farmerprocurement.backend.config;

import com.farmerprocurement.backend.security.JwtAuthenticationFilter;
import com.farmerprocurement.backend.security.IpRateLimitFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

import java.util.List;
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final IpRateLimitFilter ipRateLimitFilter;
    private final UserDetailsService userDetailsService;
    private final String frontendOrigin;
    private final boolean secureCookie;
    private final String cookieSameSite;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            IpRateLimitFilter ipRateLimitFilter,
            UserDetailsService userDetailsService,
            @Value("${app.frontend-origin:http://localhost:5173}") String frontendOrigin,
            @Value("${app.auth-cookie-secure:false}") boolean secureCookie,
            @Value("${app.auth-cookie-same-site:Strict}") String cookieSameSite) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.ipRateLimitFilter = ipRateLimitFilter;
        this.userDetailsService = userDetailsService;
        this.frontendOrigin = frontendOrigin;
        this.secureCookie = secureCookie;
        this.cookieSameSite = cookieSameSite;
    }

    // Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication Provider
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    // Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    // Security Filter Chain
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(request ->
                                request.getHeader("Authorization") != null))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .authenticationProvider(authenticationProvider())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                .authorizeHttpRequests(auth -> auth

                        // Public Authentication APIs
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/verify-email",
                                "/api/auth/login",
                                "/api/auth/logout"
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/csrf"
                        ).permitAll()

                        // Public URLs
                        .requestMatchers(
                                "/",
                                "/error"
                        ).permitAll()

                        // Dashboard - CENTER_OPERATOR or ADMIN
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/dashboard/**"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Centers - GET
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/centers",
                                "/api/centers/**"
                        ).hasAnyRole(
                                "FARMER",
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Centers - ADMIN only
                        .requestMatchers(
                                "/api/centers/**"
                        ).hasRole("ADMIN")

                        // Procurement Slots - GET
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/slots",
                                "/api/slots/**"
                        ).hasAnyRole(
                                "FARMER",
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Procurement Slots - ADMIN only
                        .requestMatchers(
                                "/api/slots/**"
                        ).hasRole("ADMIN")

                        // Join Queue - FARMER
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/queue/join"
                        ).hasRole("FARMER")

                        // Call Next Farmer - CENTER_OPERATOR or ADMIN
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/queue/call-next"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Start Processing - CENTER_OPERATOR or ADMIN
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/queue/*/start-processing"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Complete Procurement - CENTER_OPERATOR or ADMIN
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/queue/*/complete"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Farmer's Own Queue
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/queue/my-queue"
                        ).hasRole("FARMER")

                        // Queue By Slot
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/queue/slot/**"
                        ).hasAnyRole(
                                "FARMER",
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Other Queue GET APIs
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/queue/**"
                        ).hasAnyRole(
                                "FARMER",
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Procurement Management - CREATE
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/procurements"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Procurement Management - GET ALL
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/procurements"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Procurement Management - GET BY STATUS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/procurements/status/**"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Procurement Management - GET BY ID
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/procurements/*"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Payment Management - CREATE
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/payments"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Payment Management - GET ALL
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payments"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Payment Management - GET BY STATUS
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payments/status/**"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // Payment Management - GET BY ID
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/payments/*"
                        ).hasAnyRole(
                                "CENTER_OPERATOR",
                                "ADMIN"
                        )

                        // All Remaining APIs Require Authentication
                        .anyRequest().authenticated()
                )

                // IP Rate Limit Filter
                .addFilterBefore(
                        ipRateLimitFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                // JWT Filter
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieCustomizer(cookie -> cookie
                .sameSite(cookieSameSite)
                .secure(secureCookie));
        return repository;
    }
}