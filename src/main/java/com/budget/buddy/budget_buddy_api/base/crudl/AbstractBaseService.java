package com.budget.buddy.budget_buddy_api.base.crudl;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base service class providing common CRUDL operations for entities. Subclasses can extend this class to implement specific business logic for different entity types.
 *
 * @param <E> Entity type extending BaseEntity
 * @param <R> Read model type (DTO)
 * @param <C> Create request type (DTO)
 * @param <U> Update request type (DTO)
 */
@SuppressWarnings("java:S119")
@Slf4j
@Transactional
@RequiredArgsConstructor
public abstract class AbstractBaseService<E extends BaseEntity<ID>, ID, R, C, U>
    implements BaseService<ID, R, C, U> {

  private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found with id: %s";

  @Getter
  private final BaseRepository<E, ID> repository;

  @Getter
  private final BaseMapper<E, R, C, U, ?> mapper;

  @Override
  public R create(C createRequest) {
    log.debug("Create entity: {}", createRequest);
    E savedEntity = createInternal(createRequest);
    log.debug("Saved entity: {}", savedEntity);
    return mapper.toModel(savedEntity);
  }

  @Transactional(readOnly = true)
  @Override
  public R read(ID id) {
    log.debug("Read entity by id: {}", id);
    E entity = readInternal(id);
    return mapper.toModel(entity);
  }

  @Override
  public R update(ID id, U patchRequest) {
    log.debug("Update entity by id: {}", id);
    E updatedEntity = updateInternal(id, patchRequest);
    log.debug("Updated entity: {}", updatedEntity);
    return mapper.toModel(updatedEntity);
  }

  @Override
  public void delete(ID id) {
    log.debug("Delete entity by id: {}", id);
    deleteInternal(id);
    log.debug("Successfully deleted entity with id: {}", id);
  }

  @Transactional(readOnly = true)
  @Override
  public List<R> list() {
    log.debug("List all entities");
    List<E> entities = listInternal();
    log.debug("Found {} entities", entities.size());
    return mapper.toModelList(entities);
  }

  @Transactional(readOnly = true)
  @Override
  public Page<R> list(Pageable pageable) {
    log.debug("List all entities with pageRequest: {}", pageable);
    return listInternal(pageable)
        .map(mapper::toModel);
  }

  @Transactional(readOnly = true)
  @Override
  public long count() {
    log.debug("Count all entities");
    return repository.count();
  }

  protected E createInternal(C createRequest) {
    E entity = mapper.toEntity(createRequest);
    return repository.save(entity);
  }

  protected E readInternal(ID id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  protected E updateInternal(ID id, U updateRequest) {
    E existingEntity = readInternal(id);
    mapper.patchEntity(updateRequest, existingEntity);
    return repository.save(existingEntity);
  }

  protected void deleteInternal(ID id) {
    var entity = readInternal(id);
    repository.delete(entity);
  }

  protected List<E> listInternal() {
    return repository.findAll();
  }

  protected Page<E> listInternal(Pageable pageRequest) {
    return repository.findAll(pageRequest);
  }

}
