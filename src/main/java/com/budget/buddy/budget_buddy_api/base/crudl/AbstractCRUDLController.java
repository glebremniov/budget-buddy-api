package com.budget.buddy.budget_buddy_api.base.crudl;

import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;

public abstract class AbstractCRUDLController<E extends BaseEntity<ID>, ID, R, C, U, L> {

  private final AbstractCRUDLService<E, ID, R, C, U> service;

  protected AbstractCRUDLController(AbstractCRUDLService<E, ID, R, C, U> service) {
    this.service = service;
  }

  public ResponseEntity<R> createInternal(C createRequest) {
    var created = service.create(createRequest);
    return ResponseEntity
        .created(createdURI(created))
        .body(created);
  }

  public ResponseEntity<R> readInternal(String id) {
    var item = service.read(fromString(id));
    return ResponseEntity.ok(item);
  }

  public ResponseEntity<R> updateInternal(String id, U updateRequest) {
    var updated = service.update(fromString(id), updateRequest);
    return ResponseEntity.ok(updated);
  }

  public ResponseEntity<Void> deleteInternal(String id) {
    service.delete(fromString(id));
    return ResponseEntity.noContent().build();
  }

  public ResponseEntity<L> listInternal(Integer limit, Integer offset) {
    var items = service.list(limit, offset);
    var total = service.count();

    var meta = new PaginationMeta();
    meta.setLimit(limit);
    meta.setOffset(offset);
    meta.setTotal((int) total);

    var response = listResponse(items, meta);

    return ResponseEntity.ok(response);
  }

  protected abstract URI createdURI(R created);

  protected abstract L listResponse(List<R> items, PaginationMeta meta);

  protected abstract ID fromString(String id);

}
