package com.budget.buddy.budget_buddy_api.base.config;

import com.budget.buddy.budget_buddy_api.base.TimestampToOffsetDateTimeConverter;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;

@Configuration
public class ApplicationConfig {

  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }

  @Bean
  Supplier<UUID> idGenerator() {
    return UUID::randomUUID;
  }

  @Bean
  JdbcCustomConversions customConversions(Clock clock) {
    return new JdbcCustomConversions(List.of(new TimestampToOffsetDateTimeConverter(clock)));
  }

}
