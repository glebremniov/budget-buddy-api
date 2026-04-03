package com.budget.buddy.budget_buddy_api.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "security.access-token")
@Validated
public record JwtProperties(
    @NotBlank
    @Size(min = 32, message = "must be at least 32 characters for HS256")
    String secret,
    long validitySeconds
) {

}
