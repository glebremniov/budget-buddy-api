package com.budget.buddy.budget_buddy_api.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.generated.model.AuthToken;
import com.budget.buddy.budget_buddy_api.generated.model.RegisterRequest;
import com.budget.buddy.budget_buddy_api.security.auth.token.AuthTokenService;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenEntity;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenService;
import com.budget.buddy.budget_buddy_api.user.UserDto;
import com.budget.buddy.budget_buddy_api.user.UserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;
  @Mock
  private UserService userService;
  @Mock
  private RefreshTokenService refreshTokenService;
  @Mock
  private AuthTokenService authTokenService;

  @InjectMocks
  private AuthService authService;

  @Nested
  class RegisterTests {

    @Test
    void should_Register_When_UsernameIsAvailable() {
      // Given
      var request = new RegisterRequest("newuser", "password123");
      when(userService.existsByUsername(request.getUsername())).thenReturn(false);

      // When
      authService.register(request);

      // Then
      verify(userService).create(request);
    }

    @Test
    void should_ThrowException_When_UsernameAlreadyTaken() {
      // Given
      var request = new RegisterRequest("takenuser", "password123");
      when(userService.existsByUsername(request.getUsername())).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> authService.register(request))
          .as("Should throw DataIntegrityViolationException when the username is already taken")
          .isInstanceOf(DataIntegrityViolationException.class)
          .hasMessageContaining("Username already taken: takenuser");

      verify(userService).existsByUsername("takenuser");
      verifyNoInteractions(authTokenService);
    }
  }

  @Nested
  class LoginTests {

    @Test
    void should_Login_When_CredentialsAreValid() {
      // Given
      var username = "user";
      var password = "password";
      var userId = UUID.randomUUID();
      var userDto = new UserDto(userId, username, true);
      var authToken = new AuthToken();

      when(userService.findByUsername(username)).thenReturn(Optional.of(userDto));
      when(authTokenService.createToken(userDto)).thenReturn(authToken);

      // When
      var result = authService.login(username, password);

      // Then
      assertThat(result)
          .as("Login result should be the expected auth token")
          .isSameAs(authToken);

      var authCaptor = ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
      verify(authenticationManager).authenticate(authCaptor.capture());

      assertThat(authCaptor.getValue().getPrincipal())
          .as("Authentication principal should be the username")
          .isEqualTo(username);

      assertThat(authCaptor.getValue().getCredentials())
          .as("Authentication credentials should be the password")
          .isEqualTo(password);

      verify(userService).findByUsername(username);
      verify(authTokenService).createToken(userDto);
    }

    @Test
    void should_ThrowException_When_UserNotFoundAfterAuthentication() {
      // Given
      var username = "user";
      var password = "password";
      when(userService.findByUsername(username)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> authService.login(username, password))
          .as("Should throw UsernameNotFoundException if user is not found after authentication")
          .isInstanceOf(UsernameNotFoundException.class);
    }
  }

  @Nested
  class RefreshTests {

    @Test
    void should_RefreshToken_When_TokenIsValid() {
      // Given
      var refreshToken = "valid-refresh-token";
      var userId = UUID.randomUUID();
      var tokenEntity = RefreshTokenEntity.builder().userId(userId).token(refreshToken).build();
      var userDto = new UserDto(userId, "user", true);
      var newAuthToken = new AuthToken();

      when(refreshTokenService.rotate(refreshToken)).thenReturn(tokenEntity);
      when(userService.requireEnabledUser(userId)).thenReturn(userDto);
      when(authTokenService.createToken(userDto)).thenReturn(newAuthToken);

      // When
      var result = authService.refresh(refreshToken);

      // Then
      assertThat(result)
          .as("Refresh result should be the new auth token")
          .isSameAs(newAuthToken);

      verify(refreshTokenService).rotate(refreshToken);
      verify(userService).requireEnabledUser(userId);
      verify(authTokenService).createToken(userDto);
    }
  }

  @Nested
  class LogoutTests {

    @BeforeEach
    void setUp() {
      SecurityContextHolder.clearContext();
    }

    @Test
    void should_Logout_When_UserIsAuthenticated() {
      // Given
      var userId = UUID.randomUUID();
      var jwt = mock(Jwt.class);
      when(jwt.getSubject()).thenReturn(userId.toString());

      var authentication = mock(Authentication.class);
      when(authentication.getPrincipal()).thenReturn(jwt);

      var securityContext = mock(SecurityContext.class);
      when(securityContext.getAuthentication()).thenReturn(authentication);
      SecurityContextHolder.setContext(securityContext);

      // When
      authService.logout();

      // Then
      verify(refreshTokenService).revokeAll(userId);
    }
  }
}
