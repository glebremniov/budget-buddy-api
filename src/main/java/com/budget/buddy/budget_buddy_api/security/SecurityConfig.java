package com.budget.buddy.budget_buddy_api.security;

import com.budget.buddy.budget_buddy_api.security.oidc.OidcUserProvisioningFilter;
import com.budget.buddy.budget_buddy_api.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for the application.
 * The API is a stateless OIDC resource server — all authentication is handled
 * by the external OIDC provider. JWTs are validated against the configured issuer.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      CorsConfigurationSource corsConfigurationSource,
      UserService userService
  ) {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
        .addFilterAfter(new OidcUserProvisioningFilter(userService), BearerTokenAuthenticationFilter.class)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(csrf -> csrf.ignoringRequestMatchers("/v1/**", "/actuator/**"))
        .formLogin(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
