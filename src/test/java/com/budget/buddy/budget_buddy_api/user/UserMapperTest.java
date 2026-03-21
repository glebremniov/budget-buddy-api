package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserMapper userMapper;

  @Nested
  class ToEntity {

    @Test
    void shouldMapRegisterRequestToUserEntity() {
      // Given
      var request = new RegisterRequest();
      request.setUsername("testuser");
      request.setPassword("password123");
      var encodedPassword = "encoded_password";

      when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);

      // When
      var entity = userMapper.toEntity(request);

      // Then
      assertThat(entity).isNotNull();
      assertThat(entity.getUsername()).isEqualTo(request.getUsername());
      assertThat(entity.getPassword()).isEqualTo(encodedPassword);
      assertThat(entity.isEnabled()).isTrue();
    }
  }

  @Nested
  class ToModel {

    @Test
    void shouldMapUserEntityToUserDto() {
      // Given
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .id(userId)
          .username("testuser")
          .enabled(true)
          .password("some-password")
          .build();

      // When
      var dto = userMapper.toModel(entity);

      // Then
      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo(userId);
      assertThat(dto.username()).isEqualTo(entity.getUsername());
      assertThat(dto.enabled()).isTrue();
    }
  }

  @Nested
  class UnsupportedOperations {

    @Test
    void toModelListShouldThrowException() {
      assertThatThrownBy(() -> userMapper.toModelList(null))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void toPageResponseShouldThrowException() {
      assertThatThrownBy(() -> userMapper.toPageResponse(null, null))
          .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void patchEntityShouldThrowException() {
      assertThatThrownBy(() -> userMapper.patchEntity(null, null))
          .isInstanceOf(UnsupportedOperationException.class);
    }
  }
}
