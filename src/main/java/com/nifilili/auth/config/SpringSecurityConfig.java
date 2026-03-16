package com.nifilili.auth.config;

import jakarta.servlet.DispatcherType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@AllArgsConstructor
@Slf4j
public class SpringSecurityConfig {

    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    private JwtAuthenticationFilter authenticationFilter;

    private RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // JWT-based API security: no sessions, no HTTP Basic challenge flow.
        http.csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> {
                    authorize.dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll();
                    // Public endpoints are explicitly allowlisted.
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/otp/request").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/otp/verify").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll();
                    // Account security: public endpoints for password reset and email verification
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/account/security/request-password-reset").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/account/security/reset-password-link").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/account/security/reset-password-otp").permitAll();
                    authorize.requestMatchers(HttpMethod.POST, "/api/v1/account/security/verify-email").permitAll();
                    authorize.requestMatchers("/api/v1/public/**").permitAll();
                    authorize.requestMatchers("/error").permitAll();
                    authorize.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                    // Every other API route requires authentication.
                    authorize.anyRequest().authenticated();
                });

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        log.info("Security filter chain initialized with stateless JWT policy");

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
