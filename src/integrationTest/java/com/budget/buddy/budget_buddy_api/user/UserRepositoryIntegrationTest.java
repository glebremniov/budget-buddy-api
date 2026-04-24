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
  void upsert_Should_InsertAndReturnId_When_NoUserWithSameSubject() {
    // Given
    var newId = UUID.randomUUID();
    var subject = "sub_" + UUID.randomUUID();
    var issuer = "test_issuer";

    // When
    UUID actual = userRepository.upsert(newId, subject, issuer);

    // Then
    assertThat(actual)
        .as("Returned ID should be equal to new ID")
        .isEqualTo(newId);
  }

  @Test
  void upsert_Should_ReturnOldId_When_UserWithSameSubject() {
    // Given
    var oldId = UUID.randomUUID();
    var subject = "sub_" + UUID.randomUUID();
    var issuer = "test_issuer";

    userRepository.upsert(oldId, subject, issuer);

    var newId = UUID.randomUUID();

    // When
    UUID actual = userRepository.upsert(newId, subject, issuer);

    // Then
    assertThat(actual)
        .as("Returned ID should be equal to old ID")
        .isEqualTo(oldId);
  }
}
