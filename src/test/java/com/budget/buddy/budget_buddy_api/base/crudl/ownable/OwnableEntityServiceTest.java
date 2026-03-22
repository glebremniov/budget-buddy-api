package com.budget.buddy.budget_buddy_api.base.crudl.ownable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
@DisplayName("OwnableEntityService Unit Tests")
class OwnableEntityServiceTest {

  @Mock
  private OwnableEntityRepository<DummyOwnableEntity, String> repository;

  @Mock
  private BaseEntityMapper<DummyOwnableEntity, Object, Object, Object, Object> mapper;

  @Mock
  private Converter<String, String> idConverter;

  private DummyOwnableService service;

  private final String ownerId = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
    service = new DummyOwnableService(repository, mapper, Collections.emptyList(), idConverter);
    setupSecurityContext(ownerId);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private void setupSecurityContext(String userId) {
    var jwt = mock(Jwt.class);
    when(jwt.getSubject()).thenReturn(userId);

    var authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(jwt);

    var securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);
    when(idConverter.convert(userId)).thenReturn(userId);
  }

  @Nested
  @DisplayName("getRequiredOwnerId")
  class GetRequiredOwnerIdTests {

    @Test
    @DisplayName("should return current user ID")
    void shouldReturnCurrentUserId() {
      var result = service.getRequiredOwnerId();
      assertThat(result).isEqualTo(ownerId);
    }
  }

  @Nested
  @DisplayName("listInternal")
  class ListInternalTests {

    @Test
    @DisplayName("should call repository.findAllByOwnerId")
    void shouldCallRepositoryFindAllByOwnerId() {
      var pageRequest = PageRequest.of(0, 10);
      var expectedPage = new PageImpl<>(List.of(new DummyOwnableEntity()));
      when(repository.findAllByOwnerId(ownerId, pageRequest)).thenReturn(expectedPage);

      var result = service.listInternal(pageRequest);

      assertThat(result).isEqualTo(expectedPage);
      verify(repository).findAllByOwnerId(ownerId, pageRequest);
    }
  }

  @Nested
  @DisplayName("readInternal")
  class ReadInternalTests {

    @Test
    @DisplayName("should return entity when found by ID and owner ID")
    void shouldReturnEntityWhenFound() {
      var id = "123";
      var entity = new DummyOwnableEntity();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(entity));

      var result = service.readInternal(id);

      assertThat(result).isEqualTo(entity);
    }

    @Test
    @DisplayName("should throw EntityNotFoundException when not found")
    void shouldThrowExceptionWhenNotFound() {
      var id = "123";
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.readInternal(id))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Entity not found with id: " + id);
    }
  }

  @Nested
  @DisplayName("deleteInternal")
  class DeleteInternalTests {

    @Test
    @DisplayName("should delete entity when found")
    void shouldDeleteWhenFound() {
      var id = "123";
      var entity = new DummyOwnableEntity();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(entity));

      service.deleteInternal(id);

      verify(repository).delete(entity);
    }
  }

  @Nested
  @DisplayName("updateInternal")
  class UpdateInternalTests {

    @Test
    @DisplayName("should patch and save entity")
    void shouldPatchAndSave() {
      var id = "123";
      var updateRequest = new Object();
      var existingEntity = new DummyOwnableEntity();
      when(repository.findByIdAndOwnerId(id, ownerId)).thenReturn(Optional.of(existingEntity));
      when(repository.save(existingEntity)).thenReturn(existingEntity);

      var result = service.updateInternal(id, updateRequest);

      assertThat(result).isEqualTo(existingEntity);
      verify(mapper).patchEntity(updateRequest, existingEntity);
      verify(repository).save(existingEntity);
    }
  }

  @Nested
  @DisplayName("existsByIdInternal")
  class ExistsByIdInternalTests {

    @Test
    @DisplayName("should return repository result")
    void shouldReturnRepositoryResult() {
      var id = "123";
      when(repository.existsByIdAndOwnerId(id, ownerId)).thenReturn(true);

      var result = service.existsByIdInternal(id);

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
      var createRequest = new Object();
      var entity = new DummyOwnableEntity();
      when(mapper.toEntity(createRequest)).thenReturn(entity);
      when(repository.save(entity)).thenReturn(entity);

      var result = service.createInternal(createRequest);

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
      when(repository.countByOwnerId(ownerId)).thenReturn(5L);

      var result = service.countInternal();

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
        Converter<String, String> idConverter
    ) {
      super(repository, mapper, entityValidators, idConverter);
    }
  }
}
