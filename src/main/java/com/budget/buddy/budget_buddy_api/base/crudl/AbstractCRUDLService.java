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
@RequiredArgsConstructor
public abstract class AbstractCRUDLService<E extends BaseEntity<ID>, ID, R, C, U> implements CRUDLService<ID, R, C, U> {

  private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found with id: %s";

  @Getter
  private final BaseRepository<E, ID> repository;

  @Getter
  private final BaseMapper<E, R, C, U> mapper;

  @Transactional
  @Override
  public R create(C createRequest) {
    E savedEntity = createInternal(createRequest);
    return mapper.toModel(savedEntity);
  }

  @Override
  public R read(ID id) {
    E entity = readInternal(id);
    return mapper.toModel(entity);
  }

  @Transactional
  @Override
  public R update(ID id, U updateRequest) {
    E updatedEntity = updateInternal(id, updateRequest);
    return mapper.toModel(updatedEntity);
  }

  @Transactional
  @Override
  public void delete(ID id) {
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException(String.format(ENTITY_NOT_FOUND_MESSAGE, id));
    }

    repository.deleteById(id);
  }

  @Override
  public List<R> list() {
    List<E> entities = listInternal();
    return mapper.toModelList(entities);
  }

  @Override
  public List<R> list(int limit, int offset) {
    List<E> entities = listInternal();
    int end = Math.min(offset + limit, entities.size());
    List<E> page = entities.subList(offset, end);
    return mapper.toModelList(page);
  }

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
    if (!repository.existsById(id)) {
      throw new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id));
    }

    E entity = mapper.toEntityForUpdate(updateRequest);
    entity.setId(id);

    return repository.save(entity);
  }

}
