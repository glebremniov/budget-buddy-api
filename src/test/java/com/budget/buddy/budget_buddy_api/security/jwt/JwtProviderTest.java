package com.budget.buddy.budget_buddy_api.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

  private static final String SUBJECT = "test user";
  private static final Instant NOW = Instant.parse("2007-12-03T10:15:30.00Z");
  private static final long VALIDITY_SECONDS = 120L;
  private static final Instant EXPIRE_AT = NOW.plusSeconds(VALIDITY_SECONDS);

  @Spy
  private Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));

  @Mock
  private JwtEncoder jwtEncoder;

  @Mock
  private Jwt jwt;

  @InjectMocks
  private JwtProvider jwtProvider;

  @Test
  void create_Should_CallEncoderWithCorrectClaims() {
    // Given
    var expectedToken = "expected token";
    when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);
    when(jwt.getTokenValue()).thenReturn(expectedToken);

    // When
    var actual = jwtProvider.create(SUBJECT, VALIDITY_SECONDS);

    // Then
    assertThat(actual).isEqualTo(expectedToken);

    var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(parametersCaptor.capture());

    assertThat(parametersCaptor.getValue())
        .extracting(JwtEncoderParameters::getClaims)
        .returns(SUBJECT, JwtClaimAccessor::getSubject)
        .returns(NOW, JwtClaimAccessor::getIssuedAt)
        .returns(EXPIRE_AT, JwtClaimAccessor::getExpiresAt);

    verify(jwt).getTokenValue();
  }

}
