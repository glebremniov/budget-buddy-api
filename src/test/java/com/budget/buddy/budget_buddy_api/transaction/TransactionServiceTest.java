package com.budget.buddy.budget_buddy_api.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock
  private TransactionRepository repository;
  @Mock
  private TransactionMapper mapper;
  @Mock
  private Converter<String, UUID> ownerIdConverter;

  private TransactionService transactionService;
  private final UUID currentUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    transactionService = new TransactionService(repository, mapper, Collections.emptySet(), ownerIdConverter);
    setupMockAuthentication();
  }

  private void setupMockAuthentication() {
    Jwt jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn(currentUserId.toString());
    when(ownerIdConverter.convert(currentUserId.toString())).thenReturn(currentUserId);

    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(jwt);

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);
  }

  @Nested
  class OwnableOperationTests {

    @Test
    void should_CreateTransaction_WithOwnerId() {
      // Given
      TransactionCreate createRequest = new TransactionCreate();
      TransactionEntity entity = new TransactionEntity();
      when(mapper.toEntity(createRequest)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(entity);
      when(mapper.toModel(entity)).thenReturn(new Transaction());

      // When
      transactionService.create(createRequest);

      // Then
      assertThat(entity.getOwnerId()).isEqualTo(currentUserId);
      verify(repository).save(entity);
    }
  }

  @Nested
  class FilteredTests {

    @ParameterizedTest
    @EnumSource(Direction.class)
    void should_ListWithFilter(Direction direction) {
      // Given
      TransactionFilter filter = TransactionFilter.of(null, null, null);
      Pageable pageable = PageRequest.of(0, 10, direction, "date");
      List<TransactionEntity> entities = List.of(new TransactionEntity());
      List<Transaction> models = List.of(new Transaction());

      when(repository.findAllByFilter(any(TransactionFilter.class), eq(pageable))).thenReturn(entities);
      when(mapper.toModelList(entities)).thenReturn(models);

      // When
      List<Transaction> result = transactionService.list(filter, pageable);

      // Then
      assertThat(result).isEqualTo(models);
      verify(repository).findAllByFilter(any(TransactionFilter.class), eq(pageable));
    }

    @Test
    void should_CountWithFilter() {
      // Given
      TransactionFilter filter = TransactionFilter.of(null, null, null);
      when(repository.countByFilter(any(TransactionFilter.class))).thenReturn(5L);

      // When
      long result = transactionService.count(filter);

      // Then
      assertThat(result).isEqualTo(5L);
      verify(repository).countByFilter(any(TransactionFilter.class));
    }
  }
}
