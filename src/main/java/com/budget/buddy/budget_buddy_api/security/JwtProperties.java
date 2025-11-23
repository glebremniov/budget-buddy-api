package com.budget.buddy.budget_buddy_api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("jwt")
public record JwtProperties(String secret, long accessTokenValiditySeconds, long refreshTokenValiditySeconds) {

}
