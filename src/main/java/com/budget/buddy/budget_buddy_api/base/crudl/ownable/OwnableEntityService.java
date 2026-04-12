package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.AbstractBaseEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for services managing entities that belong to a specific owner.
 *
 * <p>All CRUDL operations are automatically scoped to the owner returned by the
 * injected {@link OwnerIdProvider}. Subclasses inherit this scoping without any
 * additional wiring.
 *
 * @param <E> the entity type
 * @param <ID> the identifier type
 * @param <R> the read model type (DTO)
 * @param <C> the create/replace request type (DTO)
 * @param <U> the partial-update request type (DTO)
 */
public class OwnableEntityService<E extends OwnableEntity<ID>, ID, R, C, U>
    extends AbstractBaseEntityService<E, ID, R, C, U> {

  private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found with id: %s";

  @Getter(AccessLevel.PROTECTED)
  private final OwnableEntityRepository<E, ID> repository;
  @Getter(AccessLevel.PROTECTED)
  private final BaseEntityMapper<E, R, C, U, ?> mapper;
  @Getter(AccessLevel.PROTECTED)
  private final OwnerIdProvider<ID> ownerIdProvider;

  protected OwnableEntityService(
      OwnableEntityRepository<E, ID> repository,
      BaseEntityMapper<E, R, C, U, ?> mapper,
      Iterable<BaseEntityValidator<E>> entityValidators,
      OwnerIdProvider<ID> ownerIdProvider
  ) {
    super(repository, mapper, entityValidators);
    this.repository = repository;
    this.mapper = mapper;
    this.ownerIdProvider = ownerIdProvider;
  }

  @Override
  protected Page<E> listInternal(Pageable pageRequest) {
    return repository.findAllByOwnerId(ownerIdProvider.get(), pageRequest);
  }

  @Transactional
  @Override
  protected void deleteInternal(ID id) {
    var entity = readInternal(id);
    repository.delete(entity);
  }

  @Transactional
  @Override
  protected E updateInternal(ID id, U updateRequest) {
    E existingEntity = readInternal(id);
    mapper.patchEntity(updateRequest, existingEntity);
    validate(existingEntity);
    return repository.save(existingEntity);
  }

  @Transactional
  @Override
  protected E replaceInternal(ID id, C replaceRequest) {
    E existingEntity = readInternal(id);
    mapper.replaceEntity(replaceRequest, existingEntity);
    validate(existingEntity);
    return repository.save(existingEntity);
  }

  @Override
  protected E readInternal(ID id) {
    return repository.findByIdAndOwnerId(id, ownerIdProvider.get())
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  @Override
  protected boolean existsByIdInternal(ID id) {
    return repository.existsByIdAndOwnerId(id, ownerIdProvider.get());
  }

  @Transactional
  @Override
  protected E createInternal(C createRequest) {
    E entity = mapper.toEntity(createRequest);
    entity.setOwnerId(ownerIdProvider.get());
    validate(entity);
    return repository.save(entity);
  }

  @Override
  public long countInternal() {
    return repository.countByOwnerId(ownerIdProvider.get());
  }
}
