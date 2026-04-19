package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("should find user by ID")
  void shouldFindById() {
    // Given
    var user = UserEntity.builder()
        .oidcSubject("sub_" + UUID.randomUUID())
        .build();
    var saved = userRepository.save(user);
    assertThat(saved.getId()).isNotNull();

    // When
    var found = userRepository.findById(saved.getId());

    // Then
    assertThat(found)
        .isPresent()
        .get()
        .returns(saved.getId(), UserEntity::getId);
  }

  @Test
  @DisplayName("should find user by OIDC subject")
  void shouldFindByOidcSubject() {
    // Given
    var oidcSubject = "oidc_" + UUID.randomUUID();
    var user = UserEntity.builder()
        .oidcSubject(oidcSubject)
        .build();
    userRepository.save(user);

    // When
    var found = userRepository.findByOidcSubject(oidcSubject);
    var notFound = userRepository.findByOidcSubject("nonexistent");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getOidcSubject()).isEqualTo(oidcSubject);
    assertThat(notFound).isEmpty();
  }
}
