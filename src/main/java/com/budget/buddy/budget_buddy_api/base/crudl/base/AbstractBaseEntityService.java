package com.budget.buddy.budget_buddy_api.base.crudl.base;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
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
public abstract class AbstractBaseEntityService<E extends BaseEntity<ID>, ID, R, C, U>
    implements BaseEntityService<ID, R, C, U> {

  private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found with id: %s";

  private final BaseEntityRepository<E, ID> repository;
  private final BaseEntityMapper<E, R, C, U, ?> mapper;
  private final Iterable<BaseEntityValidator<E>> validators;

  protected AbstractBaseEntityService(
      BaseEntityRepository<E, ID> repository,
      BaseEntityMapper<E, R, C, U, ?> mapper
  ) {
    this(repository, mapper, Collections.emptyList());
  }

  protected AbstractBaseEntityService(
      BaseEntityRepository<E, ID> repository,
      BaseEntityMapper<E, R, C, U, ?> mapper,
      Iterable<BaseEntityValidator<E>> validators
  ) {
    this.repository = repository;
    this.mapper = mapper;
    this.validators = validators;
  }

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
    return countInternal();
  }

  @Override
  public boolean existsById(ID id) {
    return existsByIdInternal(id);
  }

  /**
   * Checks if an entity exists by its unique identifier.
   *
   * @param id the unique identifier
   * @return true if the entity exists, false otherwise
   */
  protected boolean existsByIdInternal(ID id) {
    return repository.existsById(id);
  }

  /**
   * Logic to create a new entity.
   *
   * @param createRequest the create request
   * @return the created entity
   */
  protected E createInternal(C createRequest) {
    E entity = mapper.toEntity(createRequest);
    validate(entity);
    return repository.save(entity);
  }

  /**
   * Logic to read an entity by its unique identifier.
   *
   * @param id the unique identifier
   * @return the entity
   * @throws EntityNotFoundException if the entity is not found
   */
  protected E readInternal(ID id) {
    return repository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  /**
   * Logic to update an entity.
   *
   * @param id            the unique identifier
   * @param updateRequest the update request
   * @return the updated entity
   */
  protected E updateInternal(ID id, U updateRequest) {
    E existingEntity = readInternal(id);
    mapper.patchEntity(updateRequest, existingEntity);
    validate(existingEntity);
    return repository.save(existingEntity);
  }

  /**
   * Logic to delete an entity.
   *
   * @param id the unique identifier
   */
  protected void deleteInternal(ID id) {
    var entity = readInternal(id);
    repository.delete(entity);
  }

  /**
   * Logic to list all entities.
   *
   * @return list of entities
   */
  protected List<E> listInternal() {
    return repository.findAll();
  }

  /**
   * Logic to list entities with pagination.
   *
   * @param pageRequest the page request
   * @return page of entities
   */
  protected Page<E> listInternal(Pageable pageRequest) {
    return repository.findAll(pageRequest);
  }

  /**
   * Logic to count all entities.
   *
   * @return total count
   */
  protected long countInternal() {
    return repository.count();
  }

  /**
   * Validates an entity using registered validators.
   *
   * @param entity the entity to validate
   */
  protected void validate(E entity) {
    validators.forEach(v -> v.validate(entity));
  }

}
