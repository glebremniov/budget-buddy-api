package com.budget.buddy.budget_buddy_api.base.crudl;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings("java:S119")
@RequiredArgsConstructor
public class BaseEntityListener<ENTITY extends BaseEntity<ID>, ID>
    implements BeforeConvertCallback<ENTITY> {

  private final Supplier<ID> idGenerator;

  @Override
  public ENTITY onBeforeConvert(@NonNull ENTITY entity) {
    if (entity.isNew()) {
      entity.setId(idGenerator.get());
    }

    return entity;
  }

}
