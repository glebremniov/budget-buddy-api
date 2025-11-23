package com.budget.buddy.budget_buddy_api.repository;

import com.budget.buddy.budget_buddy_api.entity.CategoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Category entity operations using Spring Data JDBC.
 */
@Repository
public interface CategoryRepository extends CrudRepository<CategoryEntity, String> {

  /**
   * Find all categories.
   *
   * @return list of all categories
   */
  List<CategoryEntity> findAll();

  /**
   * Find a category by ID.
   *
   * @param id category ID
   * @return optional containing the category if found
   */
  Optional<CategoryEntity> findById(String id);

  /**
   * Find all categories by name.
   *
   * @param name category name
   * @return list of categories with matching name
   */
  List<CategoryEntity> findByName(String name);
}
