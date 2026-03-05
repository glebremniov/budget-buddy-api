package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.security.auth.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final String USERNAME = "test-user";

  @Mock
  private UserRepository userRepository;

  @Mock
  private AuthService authService;

  @InjectMocks
  private UserService userService;

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = USERNAME)
  void getCurrentUserName_Should_Call_AuthService(String userName) {
    // Given
    var expected = Optional.ofNullable(userName);
    when(authService.getCurrentUserName()).thenReturn(expected);

    // When
    var actual = userService.getCurrentUserName();

    // Then
    assertThat(actual).isEqualTo(expected);
    verify(authService).getCurrentUserName();
  }

  @Nested
  class GetCurrentUserIdOrThrowTest {

    @Test
    void should_ReturnUserId_When_UserIsAuthenticated() {
      // Given
      var userId = java.util.UUID.randomUUID();

      var userEntity = UserEntity.builder()
          .id(userId)
          .username(USERNAME)
          .build();

      when(authService.getCurrentUserName()).thenReturn(Optional.of(USERNAME));
      when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));

      // When
      var actual = userService.getCurrentUserIdOrThrow();

      // Then
      assertThat(actual).isEqualTo(userId);
      verify(authService).getCurrentUserName();
      verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void should_ThrowException_When_UserIsNotAuthenticated() {
      // Given
      when(authService.getCurrentUserName()).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.getCurrentUserIdOrThrow())
          .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
          .hasMessage("No authenticated user found");

      verify(authService).getCurrentUserName();
      verifyNoInteractions(userRepository);
    }

    @Test
    void should_ThrowException_When_AuthenticatedUserNotFoundInDatabase() {
      // Given
      when(authService.getCurrentUserName()).thenReturn(Optional.of(USERNAME));
      when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.getCurrentUserIdOrThrow())
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessage("Authenticated user not found in database");

      verify(authService).getCurrentUserName();
      verify(userRepository).findByUsername(USERNAME);
    }
  }
}
