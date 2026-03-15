package com.budget.buddy.budget_buddy_api.base.crudl;

import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import java.net.URI;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("java:S119")
public abstract class BaseController<ID, R, C, U, L> {

  private final BaseService<ID, R, C, U> service;
  private final BaseMapper<?, R, C, U, L> mapper;

  protected BaseController(
      BaseService<ID, R, C, U> service,
      BaseMapper<?, R, C, U, L> mapper
  ) {
    this.service = service;
    this.mapper = mapper;
  }

  public ResponseEntity<R> createInternal(C createRequest) {
    var created = service.create(createRequest);
    return ResponseEntity
        .created(createdURI(created))
        .body(created);
  }

  public ResponseEntity<R> readInternal(ID id) {
    var item = service.read(id);
    return ResponseEntity.ok(item);
  }

  public ResponseEntity<R> updateInternal(ID id, U updateRequest) {
    var updated = service.update(id, updateRequest);
    return ResponseEntity.ok(updated);
  }

  public ResponseEntity<Void> deleteInternal(ID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  public ResponseEntity<L> listInternal(Integer limit, Integer offset) {
    return listInternal(PageRequest.of(offset, limit));
  }

  public ResponseEntity<L> listInternal(PageRequest pageRequest) {
    var items = service.list(pageRequest);

    var meta = new PaginationMeta();
    meta.setLimit(items.getSize());
    meta.setOffset(items.getNumber());
    meta.setTotal(items.getTotalElements());

    var response = mapper.toPageResponse(items.getContent(), meta);

    return ResponseEntity.ok(response);
  }

  protected abstract URI createdURI(R created);

}
