package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock
  private TransactionRepository repository;
  @Mock
  private TransactionMapper mapper;

  private TransactionService transactionService;
  private final UUID currentUserId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    transactionService = new TransactionService(repository, mapper, Collections.emptySet(), () -> currentUserId);
  }

  @Nested
  class OwnableOperationTests {

    @Test
    void should_CreateTransaction_WithOwnerId() {
      // Given
      var createRequest = new TransactionWrite();
      var entity = new TransactionEntity();
      var model = new Transaction();

      when(mapper.toEntity(createRequest)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(entity);
      when(mapper.toModel(entity)).thenReturn(model);

      // When
      transactionService.create(createRequest);

      // Then
      assertThat(entity.getOwnerId())
          .as("Transaction owner ID should be set to the current user ID")
          .isEqualTo(currentUserId);

      verify(repository).save(entity);
    }
  }

  @Nested
  class FilteredTests {

    @ParameterizedTest
    @EnumSource(Direction.class)
    void should_ListWithFilter(Direction direction) {
      // Given
      var filter = TransactionFilter.of(null, null, null, null);
      var pageable = PageRequest.of(0, 10, direction, "date");
      var entity = new TransactionEntity();
      var model = new Transaction();

      when(repository.findAllByFilter(any(TransactionFilter.class), any())).thenReturn(new PageImpl<>(List.of(entity)));
      when(mapper.toModel(entity)).thenReturn(model);

      // When
      var result = transactionService.list(filter, pageable);

      // Then
      assertThat(result.getContent())
          .as("Filtered transactions should match the mocked models")
          .containsExactly(model);

      var filterCaptor = ArgumentCaptor.forClass(TransactionFilter.class);
      var pageableCaptor = ArgumentCaptor.forClass(PageRequest.class);

      verify(repository).findAllByFilter(filterCaptor.capture(), pageableCaptor.capture());

      assertThat(filterCaptor.getValue().ownerId())
          .as("Filter owner ID should be set to the current user ID")
          .isEqualTo(currentUserId);

      assertThat(pageableCaptor.getValue())
          .as("Pageable should be passed correctly to the repository")
          .isEqualTo(pageable);
    }
  }
}
