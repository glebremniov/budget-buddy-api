package com.budget.buddy.budget_buddy_api.base.config;

import com.budget.buddy.budget_buddy_api.base.converters.CustomJdbcConverters;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@Configuration
@EnableJdbcAuditing
public class JdbcConfig {

  @Bean
  public DateTimeProvider offsetDateTimeProvider(Clock clock) {
    return () -> Optional.of(OffsetDateTime.now(clock));
  }

  @Bean
  public JdbcCustomConversions jdbcCustomConversions() {
    return new JdbcCustomConversions(CustomJdbcConverters.CONVERTERS);
  }

}
