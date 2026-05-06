package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.*;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper
    extends BaseEntityMapper<CategoryEntity, Category, CategoryWrite, CategoryUpdate, PaginatedCategories> {

  @Override
  @Mapping(target = "monthlyBudget", source = "monthlyBudget", qualifiedByName = "toJsonNullable")
  Category toModel(CategoryEntity entity);

  @Override
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  void patchEntity(CategoryUpdate patchRequest, @MappingTarget CategoryEntity existingEntity);

  @Mapping(target = "monthlyBudget", source = "monthlyBudget", qualifiedByName = "toJsonNullable")
  CategorySpendingRow toSpendingRow(CategorySummaryRow row);

}
