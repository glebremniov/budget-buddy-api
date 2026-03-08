package com.budget.buddy.budget_buddy_api.base.crudl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractCRUDLServiceTest {

  @Mock
  private BaseMapper<DummyEntity, Object, Object, Object, Object> mapper;

  @Mock
  private BaseRepository<DummyEntity, String> repository;

  private AbstractCRUDLService<DummyEntity, String, Object, Object, Object> service;

  @BeforeEach
  void setUp() {
    service = new DummyCRUDLService(repository, mapper);
  }

  @Nested
  class CreateTests {

    @Test
    void should_SaveEntity() {
      // Given
      var createRequest = new Object();
      var entity = new DummyEntity();
      when(mapper.toEntity(createRequest)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(entity);

      // When
      service.create(createRequest);

      // Then
      verify(mapper).toEntity(createRequest);
      verify(repository).save(entity);
    }
  }

  @Nested
  class ReadTests {

    @Test
    void should_FindEntity() {
      // Given
      var id = "testId";
      var entity = new DummyEntity();
      var expected = new Object();
      when(repository.findById(id)).thenReturn(Optional.of(entity));
      when(mapper.toModel(entity)).thenReturn(expected);

      // When
      var actual = service.read(id);

      // Then
      assertThat(actual).isEqualTo(expected);
      verify(repository).findById(id);
    }

    @Test
    void should_ThrowException_When_EntityNotFound() {
      // Given
      var id = "nonExistentId";
      when(repository.findById(id)).thenReturn(Optional.empty());

      // When / Then
      assertThatThrownBy(() -> service.read(id))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Entity not found with id: " + id);
      verify(repository).findById(id);
      verifyNoInteractions(mapper);
    }

  }

  @Nested
  class UpdateTests {

    @Test
    void should_SaveEntity() {
      // Given
      var id = "testId";
      var existingEntity = new DummyEntity();
      existingEntity.setId(existingEntity.getId());
      existingEntity.setVersion(1);
      existingEntity.setCreatedAt(OffsetDateTime.now().minusDays(1));
      existingEntity.setUpdatedAt(OffsetDateTime.now());

      var updateRequest = new Object();
      var updatedEntity = new DummyEntity();
      var expected = new Object();
      when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
      when(mapper.toEntityForUpdate(updateRequest)).thenReturn(updatedEntity);
      when(repository.save(updatedEntity)).thenReturn(updatedEntity);
      when(mapper.toModel(updatedEntity)).thenReturn(expected);

      // When
      var actual = service.update(id, updateRequest);

      // Then
      assertThat(actual).isEqualTo(expected);
      assertThat(updatedEntity)
          .returns(existingEntity.getId(), BaseEntity::getId)
          .returns(existingEntity.getVersion(), BaseEntity::getVersion)
          .returns(existingEntity.getCreatedAt(), BaseEntity::getCreatedAt)
          .returns(existingEntity.getUpdatedAt(), BaseEntity::getUpdatedAt);

      verify(repository).findById(id);
      verify(mapper).toEntityForUpdate(updateRequest);
      verify(repository).save(updatedEntity);
      verify(mapper).toModel(updatedEntity);
    }

    @Test
    void should_ThrowException_When_EntityNotFound() {
      // Given
      var id = "nonExistentId";
      var updateRequest = new Object();
      when(repository.findById(id)).thenReturn(Optional.empty());

      // When / Then
      assertThatThrownBy(() -> service.update(id, updateRequest))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Entity not found with id: " + id);
      verify(repository).findById(id);
      verifyNoInteractions(mapper);
    }
  }

  @Nested
  class DeleteTests {

    @Test
    void should_DeleteEntity_When_EntityExists() {
      // Given
      var id = "testId";
      var entity = new DummyEntity();
      when(repository.findById(id)).thenReturn(Optional.of(entity));

      // When
      assertThatNoException()
          .isThrownBy(() -> service.delete(id));

      // Then
      verify(repository).delete(entity);
      verify(repository).findById(id);
    }

    @Test
    void should_ThrowException_When_EntityNotFound() {
      // Given
      var id = "testId";
      when(repository.findById(id)).thenReturn(Optional.empty());

      // When
      assertThatThrownBy(() -> service.delete(id))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Entity not found with id: " + id);

      // Then
      verify(repository).findById(id);
      verifyNoMoreInteractions(repository);
    }
  }

  @Nested
  class ListTests {

    @Test
    void should_ReturnListOfModels() {
      // Given
      var entities = List.of(new DummyEntity(), new DummyEntity());
      var expected = List.of(new Object(), new Object());
      when(repository.findAll()).thenReturn(entities);
      when(mapper.toModelList(entities)).thenReturn(expected);

      // When
      var actual = service.list();

      // Then
      assertThat(actual).isEqualTo(expected);
      verify(repository).findAll();
      verify(mapper).toModelList(entities);
    }

    @Test
    void should_ReturnPaginatedListOfModels() {
      // Given
      var entities = List.of(new DummyEntity(), new DummyEntity(), new DummyEntity());
      var entitiesPage = List.of(entities.get(1), entities.get(2));
      var expected = List.of(new Object(), new Object());
      when(repository.findAll()).thenReturn(entities);
      when(mapper.toModelList(entitiesPage)).thenReturn(expected);

      // When
      var actual = service.list(2, 1);

      // Then
      assertThat(actual).isEqualTo(expected);
      verify(repository).findAll();
      verify(mapper).toModelList(entitiesPage);
    }
  }

  @Nested
  class CountTests {

    @Test
    void should_ReturnCount() {
      // Given
      var expectedCount = 5L;
      when(repository.count()).thenReturn(expectedCount);

      // When
      var actualCount = service.count();

      // Then
      assertThat(actualCount).isEqualTo(expectedCount);
      verify(repository).count();
    }

  }
}
