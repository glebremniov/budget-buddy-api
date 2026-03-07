package com.budget.buddy.budget_buddy_api.security.jwt;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

  private final Clock clock;
  private final JwtProperties jwtProperties;

  @Bean
  JwtProvider accessTokenProvider() {
    return new JwtProvider(clock, jwtProperties.accessToken());
  }

  @Bean
  JwtProvider refreshTokenProvider() {
    return new JwtProvider(clock, jwtProperties.refreshToken());
  }

}
