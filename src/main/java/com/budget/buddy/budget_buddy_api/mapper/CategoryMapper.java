package com.budget.buddy.budget_buddy_api.mapper;

import com.budget.buddy.budget_buddy_api.entity.CategoryEntity;
import com.budget.buddy.budget_buddy_api.model.Category;
import com.budget.buddy.budget_buddy_api.model.CategoryCreate;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * Mapper for Category entities to DTO models. Handles conversion between CategoryEntity and Category models.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING
)
public interface CategoryMapper {

  /**
   * Map CategoryEntity to Category model
   */
  Category toCategory(CategoryEntity entity);

  /**
   * Map list of CategoryEntity to list of Category models
   */
  List<Category> toCategories(List<CategoryEntity> entities);

  CategoryEntity toEntity(CategoryCreate request);
}
