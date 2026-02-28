package com.budget.buddy.budget_buddy_api.base.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, long accessTokenValiditySeconds, long refreshTokenValiditySeconds) {

}
