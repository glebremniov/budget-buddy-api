package com.budget.buddy.budget_buddy_api.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.budget.buddy.budget_buddy_api.BaseIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("AuthorityRepository Integration Tests")
class AuthorityRepositoryIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private AuthorityRepository authorityRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  @DisplayName("should add default authority to user")
  void shouldAddDefaultAuthority() {
    // Given
    var username = "auth_user_" + UUID.randomUUID();
    var user = UserEntity.builder()
        .username(username)
        .password("password")
        .enabled(true)
        .build();
    userRepository.save(user);

    // When
    authorityRepository.addDefaultAuthorityToUser(username);

    // Then
    var authorities = jdbcTemplate.queryForList(
        "SELECT authority FROM authorities WHERE username = ?", String.class, username);
    assertThat(authorities).containsExactly("USER");
  }

  @Test
  @DisplayName("should add specific authority to user")
  void shouldAddSpecificAuthority() {
    // Given
    var username = "admin_user_" + UUID.randomUUID();
    var user = UserEntity.builder()
        .username(username)
        .password("password")
        .enabled(true)
        .build();
    userRepository.save(user);

    // When
    authorityRepository.addAuthorityToUser(username, "ADMIN");

    // Then
    var authorities = jdbcTemplate.queryForList(
        "SELECT authority FROM authorities WHERE username = ?", String.class, username);
    assertThat(authorities).containsExactly("ADMIN");
  }
}
