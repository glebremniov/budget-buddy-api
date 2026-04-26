package com.budget.buddy.budget_buddy_api.base.crudl.auditable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

import java.time.OffsetDateTime;

/**
 * Base class for auditable entities. Includes versioning and timestamp fields for creation and updates.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AuditableEntity {

  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";

  @Version
  @Column("version")
  private Integer version;

  @CreatedDate
  @Column(CREATED_AT)
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column(UPDATED_AT)
  private OffsetDateTime updatedAt;

}
