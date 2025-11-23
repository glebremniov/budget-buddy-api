package com.budget.buddy.budget_buddy_api.entity;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Category entity representing a budget category for organizing transactions. Uses Spring Data JDBC for data access.
 */
@Table("categories")
public class CategoryEntity extends BaseEntity {

  @Column("name")
  private String name;

  // Constructors
  public CategoryEntity() {
  }

  public CategoryEntity(String id, String name, Integer plannedAmount) {
    super.setId(id);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
