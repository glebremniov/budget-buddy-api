package com.budget.buddy.budget_buddy_api.security.cors;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "security.cors")
@Validated
public record CorsProperties(
    List<String> allowedOriginPatterns,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    boolean allowCredentials,
    long maxAge
) {

}
