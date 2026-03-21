package com.budget.buddy.budget_buddy_api.security.refresh.token;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntity;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Refresh token entity stored in the database. Allows token invalidation and rotation.
 */
@Table("refresh_tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenEntity implements BaseEntity<UUID> {

  @Id
  @Column("id")
  private UUID id;

  @Column("token")
  private String token;

  @Column("user_id")
  private UUID userId;

  @Column("created_at")
  private OffsetDateTime createdAt;

  @Column("expires_at")
  private OffsetDateTime expiresAt;
}
