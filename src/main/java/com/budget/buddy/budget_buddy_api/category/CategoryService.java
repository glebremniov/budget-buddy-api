package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.AbstractBaseService;
import com.budget.buddy.budget_buddy_api.base.exception.EntityNotFoundException;
import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryCreate;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_api.security.auth.AuthUtils;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for category operations.
 */
@Service
public class CategoryService extends AbstractBaseService<CategoryEntity, UUID, Category, CategoryCreate, CategoryUpdate> {

  private final CategoryRepository repository;
  private final CategoryMapper mapper;

  public CategoryService(CategoryRepository repository, CategoryMapper mapper) {
    super(repository, mapper);
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public long count() {
    var ownerId = AuthUtils.requireCurrentUserId();
    return repository.countByOwnerId(ownerId);
  }

  @Override
  @Transactional
  protected CategoryEntity createInternal(CategoryCreate createRequest) {
    var ownerId = AuthUtils.requireCurrentUserId();
    var entity = mapper.toEntity(createRequest, ownerId);
    return repository.save(entity);
  }

  @Override
  protected CategoryEntity readInternal(UUID categoryId) {
    var ownerId = AuthUtils.requireCurrentUserId();
    return repository.findByIdAndOwnerId(categoryId, ownerId)
        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
  }

  @Override
  protected Page<CategoryEntity> listInternal(Pageable pageable) {
    var ownerId = AuthUtils.requireCurrentUserId();
    return repository.findAllByOwnerId(ownerId, pageable);
  }
}
