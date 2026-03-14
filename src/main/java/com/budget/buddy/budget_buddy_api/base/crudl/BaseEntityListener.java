package com.budget.buddy.budget_buddy_api.base.crudl;

import java.util.function.Supplier;
import org.jspecify.annotations.NonNull;
import org.springframework.data.relational.core.mapping.event.AbstractRelationalEventListener;
import org.springframework.data.relational.core.mapping.event.BeforeConvertEvent;

@SuppressWarnings("java:S119")
public abstract class BaseEntityListener<ENTITY extends BaseEntity<ID>, ID>
    extends AbstractRelationalEventListener<ENTITY> {

  private final Supplier<ID> idGenerator;

  protected BaseEntityListener(Supplier<ID> idGenerator) {
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

}
