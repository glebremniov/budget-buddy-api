package com.budget.buddy.budget_buddy_api.security.oidc;

import com.budget.buddy.budget_buddy_api.BaseMvcIntegrationTest;
import com.budget.buddy.budget_buddy_api.user.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@DisplayName("OIDC User Provisioning Integration Tests")
class OidcUserProvisioningIntegrationTest extends BaseMvcIntegrationTest {

  @Autowired
  private JdbcClient jdbcClient;

  @Test
  @DisplayName("should provision a new user on first authenticated request")
  void shouldProvisionNewUserOnFirstRequest() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();

    // First request — user does not exist yet
    assertThat(findByOidcSubject(oidcSubject)).isEmpty();

    var result = mvc.get().uri("/v1/categories")
        .with(jwtForUser(oidcSubject))
        .exchange();

    assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());

    // User should now exist in the database
    var user = findByOidcSubject(oidcSubject);
    assertThat(user).isPresent();
    assertThat(user.get().getOidcSubject()).isEqualTo(oidcSubject);
  }

  @Test
  @DisplayName("should reuse existing user on subsequent requests")
  void shouldReuseExistingUserOnSubsequentRequests() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();

    // First request — provisions the user
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(oidcSubject))
        .exchange();

    var firstUser = findByOidcSubject(oidcSubject);
    assertThat(firstUser).isPresent();
    var userId = firstUser.get().getId();

    // Second request — should reuse the same user
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(oidcSubject))
        .exchange();

    var secondUser = findByOidcSubject(oidcSubject);
    assertThat(secondUser).isPresent();
    assertThat(secondUser.get().getId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("should reject unauthenticated requests")
  void shouldRejectUnauthenticatedRequests() {
    var result = mvc.get().uri("/v1/categories")
        .exchange();

    assertThat(result)
        .extracting(MvcTestResult::getResponse)
        .extracting(MockHttpServletResponse::getStatus)
        .isEqualTo(HttpStatus.UNAUTHORIZED.value());
  }

  @Test
  @DisplayName("should isolate data between different OIDC subjects")
  void shouldIsolateDataBetweenDifferentSubjects() {
    var subject1 = "test-sub-" + UUID.randomUUID();
    var subject2 = "test-sub-" + UUID.randomUUID();

    // Both make requests
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(subject1))
        .exchange();
    mvc.get().uri("/v1/categories")
        .with(jwtForUser(subject2))
        .exchange();

    // Both should have separate user records
    var user1 = findByOidcSubject(subject1);
    var user2 = findByOidcSubject(subject2);

    assertThat(user1).isPresent();
    assertThat(user2).isPresent();
    assertThat(user1.get().getId())
        .isNotEqualTo(user2.get().getId());
  }

  @Test
  @DisplayName("should create separate users for the same subject from different issuers")
  void shouldCreateSeparateUsersForDifferentIssuers() {
    var sharedSubject = "test-sub-" + UUID.randomUUID();
    var issuer1 = "https://issuer-one.example.com";
    var issuer2 = "https://issuer-two.example.com";

    // Request from issuer 1
    mvc.get().uri("/v1/categories")
        .with(jwt().jwt(j -> j
            .subject(sharedSubject)
            .audience(Collections.singleton("budget-buddy-api"))
            .issuer(issuer1)))
        .exchange();

    // Request from issuer 2 (same subject, different issuer)
    mvc.get().uri("/v1/categories")
        .with(jwt().jwt(j -> j
            .subject(sharedSubject)
            .audience(Collections.singleton("budget-buddy-api"))
            .issuer(issuer2)))
        .exchange();

    // Should have two distinct user records
    var users = findAllByOidcSubject(sharedSubject);
    assertThat(users).hasSize(2);
    assertThat(users.get(0).getOidcIssuer()).isNotEqualTo(users.get(1).getOidcIssuer());
    assertThat(users.get(0).getId()).isNotEqualTo(users.get(1).getId());
  }

  @Test
  @DisplayName("should reuse user when same subject and issuer make repeated requests")
  void shouldReuseSameUserForSameSubjectAndIssuer() {
    var oidcSubject = "test-sub-" + UUID.randomUUID();
    var issuer = "https://consistent-issuer.example.com";

    var jwtPost = jwt().jwt(j -> j
        .subject(oidcSubject)
        .audience(Collections.singleton("budget-buddy-api"))
        .issuer(issuer));

    mvc.get().uri("/v1/categories").with(jwtPost).exchange();
    var firstUser = findAllByOidcSubject(oidcSubject);
    assertThat(firstUser).hasSize(1);

    mvc.get().uri("/v1/categories").with(jwtPost).exchange();
    var secondLookup = findAllByOidcSubject(oidcSubject);
    assertThat(secondLookup).hasSize(1);
    assertThat(secondLookup.getFirst().getId()).isEqualTo(firstUser.getFirst().getId());
  }

  private Optional<UserEntity> findByOidcSubject(String oidcSubject) {
    return jdbcClient.sql("SELECT * FROM users WHERE oidc_subject = ?")
        .param(oidcSubject)
        .query(UserEntity.class)
        .optional();
  }

  private List<UserEntity> findAllByOidcSubject(String oidcSubject) {
    return jdbcClient.sql("SELECT * FROM users WHERE oidc_subject = ?")
        .param(oidcSubject)
        .query(UserEntity.class)
        .list();
  }
}
