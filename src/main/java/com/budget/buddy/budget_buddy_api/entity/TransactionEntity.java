package com.budget.buddy.budget_buddy_api.entity;

import java.time.LocalDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Transaction entity representing a financial transaction. Uses Spring Data JDBC for data access.
 */
@Table("transactions")
public class TransactionEntity extends BaseEntity {

  @Column("category_id")
  private String categoryId;

  @Column("amount")
  private Integer amount;

  @Column("type")
  private String type; // "expense", "income", "transfer"

  @Column("currency")
  private String currency;

  @Column("date")
  private LocalDate date;

  @Column("description")
  private String description;

  // Constructors
  public TransactionEntity() {
  }

  public TransactionEntity(String id, Integer amount, String type, String currency, LocalDate date) {
    super.setId(id);
    this.amount = amount;
    this.type = type;
    this.currency = currency;
    this.date = date;
  }

  // Getters and Setters

  public String getCategoryId() {
    return categoryId;
  }

  public void setCategoryId(String categoryId) {
    this.categoryId = categoryId;
  }

  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
