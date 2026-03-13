package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseEntity;
import java.time.LocalDate;
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
 * Transaction entity representing a financial transaction. Uses Spring Data JDBC for data access.
 */
@Table("transactions")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class TransactionEntity extends BaseEntity<UUID> {

  @ToString.Include
  @Column("category_id")
  private UUID categoryId;

  @ToString.Include
  @Column("amount")
  private Integer amount;

  @ToString.Include
  @Column("type")
  private TransactionType type;

  @Column("currency")
  private String currency;

  @ToString.Include
  @Column("date")
  private LocalDate date;

  @Column("description")
  private String description;

}
