package com.budget.buddy.budget_buddy_api.security.oidc;

import com.budget.buddy.budget_buddy_api.user.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("OidcUserProvisioningFilter Unit Tests")
@ExtendWith(MockitoExtension.class)
class OidcUserProvisioningFilterTest {

  @Mock
  private UserService userService;

  @Mock
  private FilterChain filterChain;

  private OidcUserProvisioningFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new OidcUserProvisioningFilter(userService);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("should set user ID attribute for valid JWT with sub and iss")
  void shouldSetUserIdAttributeForValidJwt() throws ServletException, IOException {
    var subject = "user-123";
    var issuer = "https://issuer.example.com";
    var localUserId = UUID.randomUUID();

    setJwtAuthentication(subject, issuer);
    when(userService.findOrCreateByOidcSubject(subject, issuer)).thenReturn(localUserId);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(request.getAttribute(OidcUserProvisioningFilter.USER_ID_ATTRIBUTE))
        .isEqualTo(localUserId);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("should throw InvalidBearerTokenException when JWT subject is missing")
  void shouldThrowWhenSubjectMissing() {
    setJwtAuthentication(null, "https://issuer.example.com");

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(InvalidBearerTokenException.class)
        .hasMessageContaining("sub");
  }

  @Test
  @DisplayName("should throw InvalidBearerTokenException when JWT issuer is missing")
  void shouldThrowWhenIssuerMissing() {
    setJwtAuthenticationWithoutIssuer("user-123");

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
        .isInstanceOf(InvalidBearerTokenException.class)
        .hasMessageContaining("iss");
  }

  @Test
  @DisplayName("should continue filter chain without setting attribute when no authentication")
  void shouldContinueChainWhenNoAuthentication() throws ServletException, IOException {
    // SecurityContext has no authentication

    filter.doFilterInternal(request, response, filterChain);

    assertThat(request.getAttribute(OidcUserProvisioningFilter.USER_ID_ATTRIBUTE)).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("should continue filter chain without setting attribute for non-JWT authentication")
  void shouldContinueChainForNonJwtAuthentication() throws ServletException, IOException {
    var anonymousAuth = new AnonymousAuthenticationToken(
        "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(request.getAttribute(OidcUserProvisioningFilter.USER_ID_ATTRIBUTE)).isNull();
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(userService);
  }

  private void setJwtAuthentication(String subject, String issuer) {
    try {
      var jwt = Jwt.withTokenValue("mock-token")
          .header("alg", "RS256")
          .subject(subject)
          .issuer(issuer)
          .issuedAt(Instant.now())
          .expiresAt(Instant.now().plusSeconds(300))
          .build();
      SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void setJwtAuthenticationWithoutIssuer(String subject) {
    var jwt = Jwt.withTokenValue("mock-token")
        .header("alg", "RS256")
        .subject(subject)
        .claim("custom", "value")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(300))
        .build();
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }
}
