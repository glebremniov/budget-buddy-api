package com.budget.buddy.budget_buddy_api.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository repository;

  @InjectMocks
  private UserService userService;

  @Nested
  class FindOrCreateByOidcSubjectTests {

    @Test
    void should_ReturnExistingUserId_When_UserExists() {
      var oidcSubject = "existing-sub";
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .oidcSubject(oidcSubject)
          .build();
      entity.setId(userId);

      when(repository.findByOidcSubject(oidcSubject)).thenReturn(Optional.of(entity));

      var jwt = Jwt.withTokenValue("token")
          .header("alg", "RS256")
          .subject(oidcSubject)
          .build();

      var result = userService.findOrCreateByOidcSubject(oidcSubject, jwt);

      assertThat(result).isEqualTo(userId);
    }

    @Test
    void should_CreateNewUser_When_UserDoesNotExist() {
      var oidcSubject = "new-sub";
      var savedId = UUID.randomUUID();
      var savedEntity = UserEntity.builder()
          .oidcSubject(oidcSubject)
          .build();
      savedEntity.setId(savedId);

      when(repository.findByOidcSubject(oidcSubject)).thenReturn(Optional.empty());
      when(repository.save(any(UserEntity.class))).thenReturn(savedEntity);

      var jwt = Jwt.withTokenValue("token")
          .header("alg", "RS256")
          .subject(oidcSubject)
          .claim("preferred_username", "johndoe")
          .build();

      var result = userService.findOrCreateByOidcSubject(oidcSubject, jwt);

      assertThat(result).isEqualTo(savedId);
      verify(repository).save(any(UserEntity.class));
    }

    @Test
    void should_RetryFind_When_ConcurrentInsertCausesConflict() {
      var oidcSubject = "race-sub";
      var userId = UUID.randomUUID();
      var entity = UserEntity.builder()
          .oidcSubject(oidcSubject)
          .build();
      entity.setId(userId);

      when(repository.findByOidcSubject(oidcSubject))
          .thenReturn(Optional.empty())
          .thenReturn(Optional.of(entity));
      when(repository.save(any(UserEntity.class)))
          .thenThrow(new DataIntegrityViolationException("duplicate key"));

      var jwt = Jwt.withTokenValue("token")
          .header("alg", "RS256")
          .subject(oidcSubject)
          .build();

      var result = userService.findOrCreateByOidcSubject(oidcSubject, jwt);

      assertThat(result).isEqualTo(userId);
    }
  }
}
