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
@Table(CategoryEntity.TABLE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity extends AuditableEntity implements OwnableEntity<UUID> {

  public static final String TABLE = "categories";
  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String OWNER_ID = "owner_id";
  public static final String MONTHLY_BUDGET = "monthly_budget";

  @Id
  @Column(ID)
  private UUID id;

  @Column(NAME)
  private String name;

  @Column(OWNER_ID)
  private UUID ownerId;

  @Column(MONTHLY_BUDGET)
  private @Nullable Long monthlyBudget;

}
