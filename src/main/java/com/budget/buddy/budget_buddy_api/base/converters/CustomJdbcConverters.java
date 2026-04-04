package com.budget.buddy.budget_buddy_api.base.converters;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomJdbcConverters {

  public static final List<Converter<?, ?>> CONVERTERS = List.of(
      new CurrencyReadingConverter(),
      new CurrencyWritingConverter(),
      new TimestampToOffsetDateTimeConverter()
  );

  @ReadingConverter
  static class CurrencyReadingConverter implements Converter<String, Currency> {

    @Override
    public Currency convert(@NonNull String source) {
      return Currency.getInstance(source);
    }
  }

  @WritingConverter
  static class CurrencyWritingConverter implements Converter<Currency, String> {

    @Override
    public String convert(@NonNull Currency source) {
      return source.getCurrencyCode();
    }
  }

  @ReadingConverter
  static class TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(@NonNull Timestamp source) {
      return source.toInstant().atOffset(ZoneOffset.UTC);
    }
  }

}
