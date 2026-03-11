package com.budget.buddy.budget_buddy_api.base.crudl;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.springframework.data.relational.core.mapping.event.AbstractRelationalEventListener;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("java:S119")
public class BaseEntityListener<ENTITY extends BaseEntity<ID>, ID> extends AbstractRelationalEventListener<ENTITY> {

  private final Clock clock;
  private final Supplier<ID> idGenerator;

  public BaseEntityListener(Clock clock, Supplier<ID> idGenerator) {
    this.clock = clock;
    this.idGenerator = idGenerator;
  }

  @Override
  protected void onBeforeConvert(@NonNull BeforeConvertEvent<ENTITY> event) {
    super.onBeforeConvert(event);

    var entity = event.getEntity();

    if (entity.isNew()) {
      entity.setId(idGenerator.get());
    }
  }

  @Override
  protected void onBeforeSave(@NonNull BeforeSaveEvent<ENTITY> event) {
    super.onBeforeSave(event);

    var entity = event.getEntity();
    var now = OffsetDateTime.now(clock);

    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(now);
    }

    entity.setUpdatedAt(now);
  }
}
