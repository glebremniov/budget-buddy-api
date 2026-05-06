package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.Category;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategorySpendingRow;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryUpdate;
import com.budget.buddy.budget_buddy_contracts.generated.model.CategoryWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedCategories;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = {JsonNullable.class})
public interface CategoryMapper
    extends BaseEntityMapper<CategoryEntity, Category, CategoryWrite, CategoryUpdate, PaginatedCategories> {

  @Override
  @Mapping(target = "monthlyBudget", expression = "java(JsonNullable.of(entity.getMonthlyBudget()))")
  Category toModel(CategoryEntity entity);

  @Override
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "ownerId", ignore = true)
  void patchEntity(CategoryUpdate patchRequest, @MappingTarget CategoryEntity existingEntity);

  @Mapping(target = "monthlyBudget", expression = "java(JsonNullable.of(row.monthlyBudget()))")
  CategorySpendingRow toSpendingRow(CategorySummaryRow row);

}
