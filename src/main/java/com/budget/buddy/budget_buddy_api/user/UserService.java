package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractBaseService;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class UserService extends AbstractBaseService<UserEntity, UUID, UserDto, RegisterRequest, Object> {

  private final UserRepository repository;
  private final UserMapper mapper;
  private final AuthorityRepository authorityRepository;

  public UserService(UserRepository repository, UserMapper mapper, AuthorityRepository authorityRepository) {
    super(repository, mapper);
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
    return null;
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
  public List<UserDto> list(int page, int size) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long count() {
    throw new UnsupportedOperationException();
  }
}
