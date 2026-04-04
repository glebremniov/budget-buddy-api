package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginationMeta;
import com.budget.buddy.budget_buddy_contracts.generated.model.RegisterRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper implements BaseEntityMapper<UserEntity, UserDto, RegisterRequest, Object, Object> {

  private final PasswordEncoder passwordEncoder;

  @Override
  public UserEntity toEntity(RegisterRequest request) {
    return UserEntity.builder()
        .username(request.getUsername())
        .password(passwordEncoder.encode(request.getPassword()))
        .enabled(true)
        .build();
  }

  @Override
  public UserDto toModel(UserEntity entity) {
    return new UserDto(entity.getId(), entity.getUsername(), entity.isEnabled());
  }

  @Override
  public List<UserDto> toModelList(Iterable<UserEntity> entities) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object toPageResponse(List<UserDto> items, PaginationMeta meta) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void patchEntity(Object patchRequest, UserEntity existingEntity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void replaceEntity(RegisterRequest replaceRequest, UserEntity existingEntity) {
    throw new UnsupportedOperationException();
  }
}
