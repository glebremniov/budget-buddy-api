package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.auditable.AuditableEntity;
import com.budget.buddy.budget_buddy_api.base.crudl.ownable.OwnableEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.Currency;
import java.util.UUID;

/**
 * Transaction entity representing a financial transaction. Uses Spring Data JDBC for data access.
 */
@Table("transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity extends AuditableEntity implements OwnableEntity<UUID> {

  public static final String CATEGORY_ID = "category_id";
  public static final String TYPE = "type";
  public static final String DATE = "date";
  public static final String OWNER_ID = "owner_id";

  @Id
  @Column("id")
  private UUID id;

  @Column(value = CATEGORY_ID)
  private UUID categoryId;

  @Column("amount")
  private Long amount;

  @Column(TYPE)
  private TransactionType type;

  @Column("currency")
  private Currency currency;

  @Column(DATE)
  private LocalDate date;

  @Column("description")
  private String description;

  @Column(OWNER_ID)
  private UUID ownerId;

}
