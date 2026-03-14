package com.budget.buddy.budget_buddy_api.security.refresh.token;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseEntity;
import java.time.OffsetDateTime;
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
 * Refresh token entity stored in the database.
 * Allows token invalidation and rotation.
 */
@Table("refresh_tokens")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class RefreshTokenEntity extends BaseEntity<String> {

  @ToString.Include
  @Column("user_id")
  private UUID userId;

  @ToString.Include
  @Column("created_at")
  private OffsetDateTime createdAt;

  @ToString.Include
  @Column("expires_at")
  private OffsetDateTime expiresAt;
}
