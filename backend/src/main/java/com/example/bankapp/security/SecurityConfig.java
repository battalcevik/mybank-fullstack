package com.example.bankapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Comma-separated list in application.properties or env:
     * app.cors.allowed-origins=http://localhost,http://localhost:5173
     */
    @Value("${app.cors.allowed-origins:http://localhost,http://localhost:5173}")
    private String allowedOriginsCsv;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // We use stateless JWT authentication (no cookies or server-side sessions).
        // CSRF attacks rely on cookies automatically sent by the browser.
        // Because our API does not use cookie-based auth, it is safe and recommended
        // to disable CSRF protection for these REST endpoints.
        http.csrf(csrf -> csrf.disable());

        http
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // keep auth endpoints and health completely open
                        .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                // JWT filter runs before the username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService uds) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsFilter corsFilter() {
        List<String> allowedOrigins = Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .toList();

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowCredentials(true);
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.addAllowedHeader(CorsConfiguration.ALL);
        cfg.addAllowedMethod(CorsConfiguration.ALL);
        source.registerCorsConfiguration("/**", cfg);

        return new CorsFilter(source);
    }
}
