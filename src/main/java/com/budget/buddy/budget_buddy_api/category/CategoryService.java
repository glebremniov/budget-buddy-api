package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityValidator;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntityService;
import com.budget.buddy.budget_buddy_api.generated.model.Category;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_api.generated.model.CategoryUpdate;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
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
      Converter<String, UUID> ownerIdConverter
  ) {
    super(repository, mapper, validators, ownerIdConverter);
  }
}
