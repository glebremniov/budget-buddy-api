package com.budget.buddy.budget_buddy_api.base.crudl.auditable;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

/**
 * Base class for auditable entities.
 * Includes versioning and timestamp fields for creation and updates.
 */
@SuppressWarnings("java:S119")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AuditableEntity {

  @Version
  @Column("version")
  private Integer version;

  @CreatedDate
  @Column("created_at")
  private OffsetDateTime createdAt;

  @LastModifiedDate
  @Column("updated_at")
  private OffsetDateTime updatedAt;

}
