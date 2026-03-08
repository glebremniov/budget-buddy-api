package com.budget.buddy.budget_buddy_api.base.crudl;

import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import java.util.List;

public interface BaseMapper<E extends BaseEntity<?>, R, C, U, L> {

  E toEntity(C createRequest);

  E toEntityForUpdate(U updateRequest);

  R toModel(E entity);

  List<R> toModelList(Iterable<E> entities);

  L toPageResponse(List<R> items, PaginationMeta meta);

}
