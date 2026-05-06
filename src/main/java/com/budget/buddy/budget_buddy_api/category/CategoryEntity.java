package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.auditable.AuditableEntity;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Category entity representing a budget category for organizing transactions. Uses Spring Data JDBC for data access.
 */
@Table("categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity extends AuditableEntity implements OwnableEntity<UUID> {

  @Id
  @Column("id")
  private UUID id;

  @Column("name")
  private String name;

  @Column("owner_id")
  private UUID ownerId;

  @Column("monthly_budget")
  private @Nullable Long monthlyBudget;

}
