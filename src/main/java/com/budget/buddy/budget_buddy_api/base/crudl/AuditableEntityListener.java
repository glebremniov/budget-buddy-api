package com.budget.buddy.budget_buddy_api.base.crudl;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.springframework.data.relational.core.mapping.event.BeforeSaveEvent;

@SuppressWarnings("java:S119")
public abstract class AuditableEntityListener<ENTITY extends AuditableEntity<ID>, ID>
    extends BaseEntityListener<ENTITY, ID> {

  private final Clock clock;

  protected AuditableEntityListener(Supplier<ID> idGenerator, Clock clock) {
    super(idGenerator);
    this.clock = clock;
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
