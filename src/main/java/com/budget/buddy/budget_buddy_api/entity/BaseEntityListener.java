package com.budget.buddy.budget_buddy_api.entity;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.springframework.data.relational.core.mapping.event.AbstractRelationalEventListener;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

@Component
public class BaseEntityListener extends AbstractRelationalEventListener<BaseEntity> {

  private final Clock clock;
  private final Supplier<UUID> idGenerator;

  public BaseEntityListener(Clock clock, Supplier<UUID> idGenerator) {
    this.clock = clock;
    this.idGenerator = idGenerator;
  }

  @Override
  protected void onBeforeConvert(BeforeConvertEvent<BaseEntity> event) {
    super.onBeforeConvert(event);

    var entity = event.getEntity();
    if (entity.getId() == null) {
      entity.setId(idGenerator.get().toString());
    }
  }

  @Override
  protected void onBeforeSave(@NonNull BeforeSaveEvent<BaseEntity> event) {
    super.onBeforeSave(event);

    var entity = event.getEntity();
    var now = OffsetDateTime.now(clock);

    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(now);
    }

    entity.setUpdatedAt(now);
  }
}
