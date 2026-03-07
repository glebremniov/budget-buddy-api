package com.budget.buddy.budget_buddy_api.base.crudl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.relational.core.conversion.BatchingAggregateChange;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

@ExtendWith(MockitoExtension.class)
class BaseEntityListenerTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.EPOCH, Clock.systemUTC().getZone());

  @Mock
  private Supplier<String> idGenerator;
  private BaseEntityListener<DummyEntity, String> listener;

  @BeforeEach
  void setUp() {
    listener = new BaseEntityListener<>(FIXED_CLOCK, idGenerator);
  }

  @Test
  void onBeforeConvert_ShouldSetId_When_NewEntity() {
    // Given
    var entity = new DummyEntity();
    var event = new BeforeConvertEvent<>(entity);
    when(idGenerator.get()).thenReturn("newId");

    // When
    assertThatNoException()
        .isThrownBy(() -> listener.onBeforeConvert(event));

    // Then
    assertThat(entity.getId()).isEqualTo("newId");
    verify(idGenerator).get();
  }

  @Test
  void onBeforeConvert_ShouldNotSetId_When_ExistingEntity() {
    // Given
    var entity = new DummyEntity();
    entity.setId("existingId");
    var event = new BeforeConvertEvent<>(entity);

    // When
    assertThatNoException()
        .isThrownBy(() -> listener.onBeforeConvert(event));

    // Then
    assertThat(entity.getId()).isEqualTo("existingId");
    verifyNoInteractions(idGenerator);
  }

  @Test
  void onBeforeSave_ShouldSetTimestamps() {
    // Given
    var entity = new DummyEntity();
    var event = new BeforeSaveEvent<>(entity, BatchingAggregateChange.forSave(DummyEntity.class));
    var expectedDateTime = OffsetDateTime.now(FIXED_CLOCK);

    // When
    assertThatNoException()
        .isThrownBy(() -> listener.onBeforeSave(event));

    // Then
    assertThat(entity)
        .returns(expectedDateTime, BaseEntity::getCreatedAt)
        .returns(expectedDateTime, BaseEntity::getUpdatedAt);
  }

  @Test
  void onBeforeSave_ShouldUpdateUpdatedAt_When_ExistingEntity() {
    // Given
    var expectedCreatedAt = OffsetDateTime.now(FIXED_CLOCK).minusDays(1);
    var expectedUpdatedAt = OffsetDateTime.now(FIXED_CLOCK);
    var entity = new DummyEntity();
    entity.setCreatedAt(expectedCreatedAt);
    var event = new BeforeSaveEvent<>(entity, BatchingAggregateChange.forSave(DummyEntity.class));

    // When
    assertThatNoException()
        .isThrownBy(() -> listener.onBeforeSave(event));

    // Then
    assertThat(entity)
        .returns(expectedCreatedAt, BaseEntity::getCreatedAt)
        .returns(expectedUpdatedAt, BaseEntity::getUpdatedAt);
  }

}
