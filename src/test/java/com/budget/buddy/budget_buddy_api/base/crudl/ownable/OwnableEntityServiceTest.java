package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("OwnableEntityService Unit Tests")
class OwnableEntityServiceTest {

  @Mock
  private OwnableEntityRepository<DummyOwnableEntity, String> repository;

  @Mock
  private BaseEntityMapper<DummyOwnableEntity, Object, Object, Object, Object> mapper;

  private DummyOwnableService service;

  private final String ownerId = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
    service = new DummyOwnableService(repository, mapper, Collections.emptyList(), () -> ownerId);
  }

  @Nested
  @DisplayName("listInternal")
  class ListInternalTests {

    @Test
    void should_CallRepositoryFindAllByOwnerId() {
      // Given
      var pageRequest = PageRequest.of(0, 10);
      var expectedPage = new PageImpl<>(List.of(new DummyOwnableEntity()));
      when(repository.findAllByOwnerId(ownerId, pageRequest)).thenReturn(expectedPage);

      // When
      var result = service.listInternal(pageRequest);

      // Then
      assertThat(result)
          .as("Resulting page should match the one returned by the repository")
          .isEqualTo(expectedPage);

      verify(repository).findAllByOwnerId(ownerId, pageRequest);
    }
  }

  @Nested
  @DisplayName("readInternal")
  class ReadInternalTests {

    @Test
    void should_ReturnEntity_When_Found() {
      // Given
      var id = "123";
      var entity = new DummyOwnableEntity();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(entity));

      // When
      var result = service.readInternal(id);

      // Then
      assertThat(result)
          .as("Result should be the entity found by ID and owner ID")
          .isEqualTo(entity);
    }

    @Test
    void should_ThrowException_When_NotFound() {
      // Given
      var id = "123";
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> service.readInternal(id))
          .as("Should throw EntityNotFoundException when entity is not found or not owned by the current user")
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Entity not found with id: " + id);
    }
  }

  @Nested
  @DisplayName("deleteInternal")
  class DeleteInternalTests {

    @Test
    void should_Delete_When_Found() {
      // Given
      var id = "123";
      var entity = new DummyOwnableEntity();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(entity));

      // When
      service.deleteInternal(id);

      // Then
      verify(repository).delete(entity);
    }
  }

  @Nested
  @DisplayName("updateInternal")
  class UpdateInternalTests {

    @Test
    void should_PatchAndSave() {
      // Given
      var id = "123";
      var updateRequest = new Object();
      var existingEntity = new DummyOwnableEntity();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existingEntity));
      when(repository.save(existingEntity)).thenReturn(existingEntity);

      // When
      var result = service.updateInternal(id, updateRequest);

      // Then
      assertThat(result)
          .as("Result should be the updated entity")
          .isEqualTo(existingEntity);

      verify(mapper).patchEntity(updateRequest, existingEntity);
      verify(repository).save(existingEntity);
    }
  }

  @Nested
  @DisplayName("replaceInternal")
  class ReplaceInternalTests {

    @Test
    void should_ReplaceAndSave() {
      // Given
      var id = "123";
      var replaceRequest = new Object();
      var existingEntity = new DummyOwnableEntity();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existingEntity));
      when(repository.save(existingEntity)).thenReturn(existingEntity);

      // When
      var result = service.replaceInternal(id, replaceRequest);

      // Then
      assertThat(result)
          .as("Result should be the replaced entity")
          .isEqualTo(existingEntity);
      verify(mapper).replaceEntity(replaceRequest, existingEntity);
      verify(repository).save(existingEntity);
    }

    @Test
    void should_ThrowException_When_NotFound() {
      // Given
      var id = "123";
      var replaceRequest = new Object();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> service.replaceInternal(id, replaceRequest))
          .as("Should throw EntityNotFoundException when entity is not found or not owned by the current user")
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Entity not found with id: " + id);
    }
  }

  @Nested
  @DisplayName("existsByIdInternal")
  class ExistsByIdInternalTests {

    @Test
    @DisplayName("should return repository result")
    void shouldReturnRepositoryResult() {
      // Given
      var id = "123";
      when(repository.existsByIdAndOwnerId(id, ownerId)).thenReturn(true);

      // When
      var result = service.existsByIdInternal(id);

      // Then
      assertThat(result).isTrue();
      verify(repository).existsByIdAndOwnerId(id, ownerId);
    }
  }

  @Nested
  @DisplayName("createInternal")
  class CreateInternalTests {

    @Test
    @DisplayName("should map to entity, set owner ID, and save")
    void shouldCreateEntity() {
      // Given
      var createRequest = new Object();
      var entity = new DummyOwnableEntity();
      when(mapper.toEntity(createRequest)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(entity);

      // When
      var result = service.createInternal(createRequest);

      // Then
      assertThat(result).isEqualTo(entity);
      assertThat(entity.getOwnerId()).isEqualTo(ownerId);
      verify(repository).save(entity);
    }
  }

  @Nested
  @DisplayName("countInternal")
  class CountInternalTests {

    @Test
    @DisplayName("should return count by owner ID")
    void shouldReturnCount() {
      // Given
      when(repository.countByOwnerId(ownerId)).thenReturn(5L);

      // When
      var result = service.countInternal();

      // Then
      assertThat(result).isEqualTo(5L);
      verify(repository).countByOwnerId(ownerId);
    }
  }

  @Getter
  @Setter
  static class DummyOwnableEntity implements OwnableEntity<String> {

    private String id;
    private String ownerId;
  }

  static class DummyOwnableService extends OwnableEntityService<DummyOwnableEntity, String, Object, Object, Object> {

    protected DummyOwnableService(
        OwnableEntityRepository<DummyOwnableEntity, String> repository,
        BaseEntityMapper<DummyOwnableEntity, Object, Object, Object, ?> mapper,
        Iterable<BaseEntityValidator<DummyOwnableEntity>> entityValidators,
        OwnerIdProvider<String> ownerIdProvider
    ) {
      super(repository, mapper, entityValidators, ownerIdProvider);
    }
  }
}
