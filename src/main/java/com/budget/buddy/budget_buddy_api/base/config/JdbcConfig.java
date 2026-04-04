package com.budget.buddy.budget_buddy_api.base.config;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
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
    return new JdbcCustomConversions(List.of(
        new CurrencyReadingConverter(),
        new CurrencyWritingConverter(),
        new TimestampToOffsetDateTimeConverter()
    ));
  }

  @ReadingConverter
  static class CurrencyReadingConverter implements Converter<String, Currency> {
    @Override
    public Currency convert(String source) {
      return Currency.getInstance(source);
    }
  }

  @WritingConverter
  static class CurrencyWritingConverter implements Converter<Currency, String> {
    @Override
    public String convert(Currency source) {
      return source.getCurrencyCode();
    }
  }

  @ReadingConverter
  static class TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {
    @Override
    public OffsetDateTime convert(Timestamp source) {
      return source.toInstant().atOffset(ZoneOffset.UTC);
    }
  }

}
