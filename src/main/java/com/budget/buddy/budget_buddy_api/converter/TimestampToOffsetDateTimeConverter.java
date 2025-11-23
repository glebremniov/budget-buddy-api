package com.budget.buddy.budget_buddy_api.converter;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.OffsetDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class TimestampToOffsetDateTimeConverter implements Converter<Timestamp, OffsetDateTime> {

  private final Clock clock;

  public TimestampToOffsetDateTimeConverter(Clock clock) {
    this.clock = clock;
  }

  @Override
  public OffsetDateTime convert(Timestamp timestamp) {
    if (timestamp == null) {
      return null;
    }

    return OffsetDateTime.ofInstant(timestamp.toInstant(), clock.getZone());
  }
}
