package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseEntity;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Transaction entity representing a financial transaction. Uses Spring Data JDBC for data access.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table("transactions")
public class TransactionEntity extends BaseEntity<UUID> {

  @Column("category_id")
  private UUID categoryId;

  @Column("amount")
  private Integer amount;

  @Column("type")
  private TransactionType type;

  @Column("currency")
  private String currency;

  @Column("date")
  private LocalDate date;

  @Column("description")
  private String description;

}
