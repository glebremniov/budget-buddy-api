package com.budget.buddy.budget_buddy_api.security.auth.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.security.jwt.JwtProperties;
import com.budget.buddy.budget_buddy_api.security.jwt.JwtProvider;
import com.budget.buddy.budget_buddy_api.security.refresh.token.RefreshTokenService;
import com.budget.buddy.budget_buddy_api.user.UserDto;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTest {

  @Mock
  private JwtProperties jwtProperties;
  @Mock
  private JwtProvider jwtProvider;
  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private AuthTokenService authTokenService;

  @Test
  void createToken_Should_ReturnAuthToken() {
    // Given
    var userId = UUID.randomUUID();
    var userDto = new UserDto(userId, "testuser", true);
    var accessToken = "access-token";
    var refreshToken = "refresh-token";
    var validitySeconds = 3600L;

    when(jwtProperties.validitySeconds()).thenReturn(validitySeconds);
    when(jwtProvider.create(userId.toString(), validitySeconds)).thenReturn(accessToken);
    when(refreshTokenService.createToken(userDto)).thenReturn(refreshToken);

    // When
    var result = authTokenService.createToken(userDto);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getAccessToken()).isEqualTo(accessToken);
    assertThat(result.getRefreshToken()).isEqualTo(refreshToken);
    assertThat(result.getTokenType()).isEqualTo("Bearer");
    assertThat(result.getExpiresIn()).isEqualTo((int) validitySeconds);

    verify(jwtProvider).create(userId.toString(), validitySeconds);
    verify(refreshTokenService).createToken(userDto);
  }
}
