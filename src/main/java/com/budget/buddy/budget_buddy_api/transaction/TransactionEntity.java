package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.auditable.AuditableEntity;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntity;
import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Transaction entity representing a financial transaction. Uses Spring Data JDBC for data access.
 */
@Table("transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity extends AuditableEntity implements OwnableEntity<UUID> {

  @Id
  @Column("id")
  private UUID id;

  @Column("category_id")
  private UUID categoryId;

  @Column("amount")
  private Long amount;

  @Column("type")
  private TransactionType type;

  @Column("currency")
  private Currency currency;

  @Column("date")
  private LocalDate date;

  @Column("description")
  private String description;

  @Column("owner_id")
  private UUID ownerId;

}
