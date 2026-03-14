package com.budget.buddy.budget_buddy_api.category;

import com.budget.buddy.budget_buddy_api.base.crudl.AuditableEntity;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Category entity representing a budget category for organizing transactions. Uses Spring Data JDBC for data access.
 */
@Table("categories")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class CategoryEntity extends AuditableEntity<UUID> {

  @Column("name")
  private String name;

  @Column("owner_id")
  private UUID ownerId;

}
