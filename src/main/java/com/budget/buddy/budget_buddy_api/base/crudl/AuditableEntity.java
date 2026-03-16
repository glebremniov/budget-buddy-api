package com.budget.buddy.budget_buddy_api.base.crudl;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;

@SuppressWarnings("java:S119")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public abstract class AuditableEntity<ID> implements BaseEntity<ID> {

  @Id
  @Column("id")
  private ID id;

  @ToString.Include
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
