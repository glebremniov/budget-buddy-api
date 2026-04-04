package com.budget.buddy.budget_buddy_api.base.crudl.base;

import com.budget.buddy.budget_buddy_api.generated.model.PaginationMeta;
import java.net.URI;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

/**
 * Base controller class providing common internal methods for CRUD operations. Designed to be extended by controllers that implement generated API interfaces.
 *
 * @param <ID> the identifier type
 * @param <R> the read model type (DTO)
 * @param <C> the create request type (DTO)
 * @param <U> the update request type (DTO)
 * @param <L> the list response type (DTO)
 */
public abstract class BaseEntityController<ID, R, C, U, L> {

  private final BaseEntityService<ID, R, C, U> service;
  private final BaseEntityMapper<?, R, C, U, L> mapper;

  protected BaseEntityController(
      BaseEntityService<ID, R, C, U> service,
      BaseEntityMapper<?, R, C, U, L> mapper
  ) {
    this.service = service;
    this.mapper = mapper;
  }

  /**
   * Internal method to create an entity.
   *
   * @param createRequest the create request
   * @return {@link ResponseEntity} with the created entity and location header
   */
  public ResponseEntity<R> createInternal(C createRequest) {
    var created = service.create(createRequest);
    return ResponseEntity
        .created(createdURI(created))
        .body(created);
  }

  /**
   * Internal method to read an entity by ID.
   *
   * @param id the entity identifier
   * @return {@link ResponseEntity} with the entity
   */
  public ResponseEntity<R> readInternal(ID id) {
    var item = service.read(id);
    return ResponseEntity.ok(item);
  }

  /**
   * Internal method to update an entity by ID.
   *
   * @param id the entity identifier
   * @param updateRequest the update request
   * @return {@link ResponseEntity} with the updated entity
   */
  public ResponseEntity<R> updateInternal(ID id, U updateRequest) {
    var updated = service.update(id, updateRequest);
    return ResponseEntity.ok(updated);
  }

  /**
   * Internal method to fully replace an entity by ID.
   *
   * @param id the entity identifier
   * @param replaceRequest the replace request (all fields required)
   * @return {@link ResponseEntity} with the replaced entity
   */
  public ResponseEntity<R> replaceInternal(ID id, C replaceRequest) {
    var replaced = service.replace(id, replaceRequest);
    return ResponseEntity.ok(replaced);
  }

  /**
   * Internal method to delete an entity by ID.
   *
   * @param id the entity identifier
   * @return {@link ResponseEntity} with no content
   */
  public ResponseEntity<Void> deleteInternal(ID id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Internal method to list entities with limit and offset.
   *
   * @param limit the maximum number of items to return
   * @param offset the page number (0-indexed)
   * @return {@link ResponseEntity} with the paginated response
   */
  public ResponseEntity<L> listInternal(Integer limit, Integer offset) {
    return listInternal(PageRequest.of(offset, limit));
  }

  /**
   * Internal method to list entities with a {@link PageRequest}.
   *
   * @param pageRequest the page request
   * @return {@link ResponseEntity} with the paginated response
   */
  public ResponseEntity<L> listInternal(PageRequest pageRequest) {
    var items = service.list(pageRequest);

    var meta = new PaginationMeta();
    meta.setLimit(items.getSize());
    meta.setOffset(items.getNumber());
    meta.setTotal(items.getTotalElements());

    var response = mapper.toPageResponse(items.getContent(), meta);

    return ResponseEntity.ok(response);
  }

  /**
   * Generates the URI for a newly created resource.
   *
   * @param created the created resource
   * @return the URI of the resource
   */
  protected abstract URI createdURI(R created);
}
