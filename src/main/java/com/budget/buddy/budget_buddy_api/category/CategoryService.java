package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnerIdProvider;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Service for category operations.
 */
@Service
public class CategoryService extends OwnableEntityService<CategoryEntity, UUID, Category, CategoryWrite, CategoryUpdate> {

  public CategoryService(
      CategoryRepository repository,
      CategoryMapper mapper,
      Set<BaseEntityValidator<CategoryEntity>> validators,
      OwnerIdProvider<UUID> ownerIdProvider
  ) {
    super(repository, mapper, validators, ownerIdProvider);
  }
}
