package com.budget.buddy.budget_buddy_api.base.crudl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;

@ExtendWith(MockitoExtension.class)
class BaseEntityListenerTest {

  @Mock
  private Supplier<String> idGenerator;

  @InjectMocks
  private DummyBaseEntityListener listener;

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

  private static final class DummyBaseEntityListener extends BaseEntityListener<DummyEntity, String> {

    public DummyBaseEntityListener(Supplier<String> idGenerator) {
      super(idGenerator);
    }
  }
}
