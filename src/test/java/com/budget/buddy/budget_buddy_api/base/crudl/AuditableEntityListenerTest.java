package com.budget.buddy.budget_buddy_api.base.crudl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.conversion.BatchingAggregateChange;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

class AuditableEntityListenerTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.EPOCH, Clock.systemUTC().getZone());

  private AuditableEntityListener<DummyAuditableEntity, String> listener;

  @BeforeEach
  void setUp() {
    listener = new DummyAuditableEntityListener(null, FIXED_CLOCK);
  }

  @Test
  void onBeforeSave_ShouldSetTimestamps() {
    // Given
    var entity = new DummyAuditableEntity();
    var event = new BeforeSaveEvent<>(entity, BatchingAggregateChange.forSave(DummyAuditableEntity.class));
    var expectedDateTime = OffsetDateTime.now(FIXED_CLOCK);

    // When
    assertThatNoException()
        .isThrownBy(() -> listener.onBeforeSave(event));

    // Then
    assertThat(entity)
        .returns(expectedDateTime, AuditableEntity::getCreatedAt)
        .returns(expectedDateTime, AuditableEntity::getUpdatedAt);
  }

  @Test
  void onBeforeSave_ShouldUpdateUpdatedAt_When_ExistingEntity() {
    // Given
    var expectedCreatedAt = OffsetDateTime.now(FIXED_CLOCK).minusDays(1);
    var expectedUpdatedAt = OffsetDateTime.now(FIXED_CLOCK);
    var entity = new DummyAuditableEntity();
    entity.setCreatedAt(expectedCreatedAt);
    var event = new BeforeSaveEvent<>(entity, BatchingAggregateChange.forSave(DummyAuditableEntity.class));

    // When
    assertThatNoException()
        .isThrownBy(() -> listener.onBeforeSave(event));

    // Then
    assertThat(entity)
        .returns(expectedCreatedAt, AuditableEntity::getCreatedAt)
        .returns(expectedUpdatedAt, AuditableEntity::getUpdatedAt);
  }

  private static final class DummyAuditableEntity extends AuditableEntity<String> {
    // No additional fields needed for testing
  }

  private static final class DummyAuditableEntityListener extends AuditableEntityListener<DummyAuditableEntity, String> {

    public DummyAuditableEntityListener(Supplier<String> idGenerator, Clock clock) {
      super(idGenerator, clock);
    }
  }

}
