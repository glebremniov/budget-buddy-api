package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository repository;
  @Mock
  private UserMapper mapper;
  @Mock
  private AuthorityRepository authorityRepository;

  private UserService userService;

  @BeforeEach
  void setUp() {
    userService = new UserService(repository, mapper, authorityRepository, Collections.emptySet());
  }

  @Nested
  class ExistsTests {

    @Test
    void should_ReturnTrue_When_UsernameExists() {
      // Given
      var username = "existingUser";
      when(repository.existsByUsername(username)).thenReturn(true);

      // When
      var exists = userService.existsByUsername(username);

      // Then
      assertThat(exists)
          .as("Username should exist in the repository")
          .isTrue();

      verify(repository).existsByUsername(username);
    }
  }

  @Nested
  class FindByUsernameTests {

    @Test
    void should_ReturnUserDto_When_UsernameExists() {
      // Given
      var username = "testUser";
      var entity = new UserEntity();
      var dto = new UserDto(UUID.randomUUID(), username, true);

      when(repository.findByUsername(username)).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(dto);

      // When
      var result = userService.findByUsername(username);

      // Then
      assertThat(result)
          .as("Result should contain the expected UserDto")
          .contains(dto);

      verify(repository).findByUsername(username);
      verify(mapper).toModel(entity);
    }
  }

  @Nested
  class CreateInternalTests {

    @Test
    void should_CreateUserAndAddAuthority() {
      // Given
      var username = "newuser";
      var request = new RegisterRequest(username, "password");
      var entity = new UserEntity();
      entity.setUsername(username);

      when(repository.save(any(UserEntity.class))).thenReturn(entity);
      when(mapper.toEntity(request)).thenReturn(new UserEntity());

      // When
      var result = userService.createInternal(request);

      // Then
      assertThat(result)
          .as("Created user should not be null")
          .isNotNull();

      verify(repository).save(any(UserEntity.class));
      verify(authorityRepository).addDefaultAuthorityToUser(username);
    }
  }

  @Nested
  class RequireEnabledUserTests {

    @Test
    void should_ReturnUserDto_When_UserIsEnabled() {
      // Given
      var userId = UUID.randomUUID();
      var entity = new UserEntity();
      entity.setEnabled(true);
      var dto = new UserDto(userId, "user", true);

      when(repository.findById(userId)).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(dto);

      // When
      var result = userService.requireEnabledUser(userId);

      // Then
      assertThat(result)
          .as("Should return the UserDto for an enabled user")
          .isEqualTo(dto);
    }

    @Test
    void should_ThrowException_When_UserIsDisabled() {
      // Given
      var userId = UUID.randomUUID();
      var entity = new UserEntity();
      entity.setEnabled(false);

      when(repository.findById(userId)).thenReturn(Optional.of(entity));

      // When & Then
      assertThatThrownBy(() -> userService.requireEnabledUser(userId))
          .as("Should throw DisabledException for a disabled user")
          .isInstanceOf(DisabledException.class)
          .hasMessage("User is disabled");
    }
  }

  @Nested
  class UnsupportedOperationsTests {

    @Test
    void should_ThrowException_On_Update() {
      // Given
      var id = UUID.randomUUID();
      var patch = new Object();

      // When & Then
      assertThatThrownBy(() -> userService.update(id, patch))
          .as("Update operation should be unsupported for users")
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowException_On_Delete() {
      // Given
      var id = UUID.randomUUID();

      // When & Then
      assertThatThrownBy(() -> userService.delete(id))
          .as("Delete operation should be unsupported for users")
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowException_On_List() {
      assertThatThrownBy(() -> userService.list()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowException_On_ListWithPageable() {
      assertThatThrownBy(() -> userService.list(null)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void should_ThrowException_On_Count() {
      assertThatThrownBy(() -> userService.count()).isInstanceOf(UnsupportedOperationException.class);
    }
  }
}
