package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import com.budget.buddy.budget_buddy_api.base.crudl.base.AbstractBaseEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.security.auth.AuthUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Base class for services managing entities that belong to a user.
 *
 * @param <E>  the entity type
 * @param <ID> the identifier type
 * @param <R>  the read model type (DTO)
 * @param <C>  the create request type (DTO)
 * @param <U>  the update request type (DTO)
 */
@SuppressWarnings("java:S119")
public class OwnableEntityService<E extends OwnableEntity<ID>, ID, R, C, U>
    extends AbstractBaseEntityService<E, ID, R, C, U> {

  private static final String ENTITY_NOT_FOUND_MESSAGE = "Entity not found with id: %s";

  @Getter(AccessLevel.PROTECTED)
  private final OwnableEntityRepository<E, ID> repository;
  @Getter(AccessLevel.PROTECTED)
  private final BaseEntityMapper<E, R, C, U, ?> mapper;
  @Getter(AccessLevel.PROTECTED)
  private final Converter<String, ID> idConverter;

  protected OwnableEntityService(
      OwnableEntityRepository<E, ID> repository,
      BaseEntityMapper<E, R, C, U, ?> mapper,
      Iterable<BaseEntityValidator<E>> entityValidators,
      Converter<String, ID> idConverter
  ) {
    super(repository, mapper, entityValidators);
    this.repository = repository;
    this.mapper = mapper;
    this.idConverter = idConverter;
  }

  /**
   * Retrieves the current authenticated user's ID.
   *
   * @return the current user's ID
   */
  protected ID getRequiredOwnerId() {
    return AuthUtils.requireCurrentUserId(idConverter);
  }

  @Override
  protected Page<E> listInternal(Pageable pageRequest) {
    return repository.findAllByOwnerId(getRequiredOwnerId(), pageRequest);
  }

  @Override
  protected void deleteInternal(ID id) {
    var entity = readInternal(id);
    repository.delete(entity);
  }

  @Override
  protected E updateInternal(ID id, U updateRequest) {
    E existingEntity = readInternal(id);
    mapper.patchEntity(updateRequest, existingEntity);
    validate(existingEntity);
    return repository.save(existingEntity);
  }

  @Override
  protected E readInternal(ID id) {
    return repository.findByIdAndOwnerId(id, getRequiredOwnerId())
        .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE.formatted(id)));
  }

  @Override
  protected boolean existsByIdInternal(ID id) {
    return repository.existsByIdAndOwnerId(id, getRequiredOwnerId());
  }

  @Override
  protected E createInternal(C createRequest) {
    E entity = mapper.toEntity(createRequest);
    entity.setOwnerId(getRequiredOwnerId());
    validate(entity);
    return repository.save(entity);
  }

  @Override
  public long countInternal() {
    return repository.countByOwnerId(getRequiredOwnerId());
  }
}
