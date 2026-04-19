package com.budget.buddy.budget_buddy_api.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private Supplier<UUID> idGenerator;

  @InjectMocks
  private UserService userService;

  @Test
  void upsert_Should_DelegateToRepository() {
    // Given
    var subject = "test-sub";
    var userId = UUID.randomUUID();
    var existingId = UUID.randomUUID();
    when(idGenerator.get()).thenReturn(userId);
    when(userRepository.upsert(userId, subject)).thenReturn(existingId);

    // When
    UUID actual = userService.findOrCreateByOidcSubject(subject);

    // Then
    assertThat(actual).isEqualTo(existingId);
  }

}
