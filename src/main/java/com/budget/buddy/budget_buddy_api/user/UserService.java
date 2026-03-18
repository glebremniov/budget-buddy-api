package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.base.AbstractBaseEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class UserService extends AbstractBaseEntityService<UserEntity, UUID, UserDto, RegisterRequest, Object> {

  private final UserRepository repository;
  private final UserMapper mapper;
  private final AuthorityRepository authorityRepository;

  public UserService(
      UserRepository repository,
      UserMapper mapper,
      AuthorityRepository authorityRepository,
      Set<BaseEntityValidator<UserEntity>> validators
  ) {
    super(repository, mapper, validators);
    this.repository = repository;
    this.mapper = mapper;
    this.authorityRepository = authorityRepository;
  }

  public boolean existsByUsername(String username) {
    return repository.existsByUsername(username);
  }

  public Optional<UserDto> findByUsername(String username) {
    return repository.findByUsername(username)
        .map(mapper::toModel);
  }

  @Transactional
  @Override
  protected UserEntity createInternal(RegisterRequest createRequest) {
    var savedUser = super.createInternal(createRequest);
    authorityRepository.addDefaultAuthorityToUser(savedUser.getUsername());
    return savedUser;
  }

  @Override
  public UserDto update(UUID uuid, Object patchRequest) throws EntityNotFoundException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(UUID uuid) throws EntityNotFoundException {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<UserDto> list() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Page<UserDto> list(Pageable pageRequest) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException();
  }

  /**
   * Find and validate that user exists and is enabled
   *
   * @param userId user ID
   * @return UserDto if user exists and is enabled
   * @throws DisabledException if user is disabled
   */
  public UserDto requireEnabledUser(UUID userId) {
    var user = readInternal(userId);
    if (!user.isEnabled()) {
      throw new DisabledException("User is disabled");
    }
    return mapper.toModel(user);
  }
}
