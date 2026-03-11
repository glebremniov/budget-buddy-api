package com.budget.buddy.budget_buddy_api.base.crudl;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base service class providing common CRUDL operations for entities. Subclasses can extend this class to implement specific business logic for different entity types.
 *
 * @param <E> Entity type extending BaseEntity
 * @param <R> Read model type (DTO)
 * @param <C> Create request type (DTO)
 * @param <U> Update request type (DTO)
 */
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
    E savedEntity = createInternal(createRequest);
    return mapper.toModel(savedEntity);
  }

  @Transactional(readOnly = true)
  @Override
  public R read(ID id) {
    E entity = readInternal(id);
    return mapper.toModel(entity);
  }

  @Override
  public R update(ID id, U patchRequest) {
    E updatedEntity = updateInternal(id, patchRequest);
    return mapper.toModel(updatedEntity);
  }

  @Override
  public void delete(ID id) {
    var entity = readInternal(id);
    repository.delete(entity);
  }

  @Transactional(readOnly = true)
  @Override
  public List<R> list() {
    List<E> entities = listInternal();
    return mapper.toModelList(entities);
  }

  @Transactional(readOnly = true)
  @Override
  public List<R> list(int limit, int offset) {
    List<E> entities = listInternal();
    int end = Math.min(offset + limit, entities.size());
    List<E> page = entities.subList(offset, end);
    return mapper.toModelList(page);
  }

  @Transactional(readOnly = true)
  @Override
  public long count() {
    return repository.count();
  }

  protected E readInternal(ID id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  protected List<E> listInternal() {
    return repository.findAll();
  }

  protected E createInternal(C createRequest) {
    E entity = mapper.toEntity(createRequest);
    return repository.save(entity);
  }

  protected E updateInternal(ID id, U updateRequest) {
    E existingEntity = readInternal(id);
    mapper.patchEntity(updateRequest, existingEntity);
    return repository.save(existingEntity);
  }

}
