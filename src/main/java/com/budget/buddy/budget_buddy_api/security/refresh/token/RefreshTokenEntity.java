package com.budget.buddy.budget_buddy_api.security.refresh.token;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Refresh token entity stored in the database. Allows token invalidation and rotation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("refresh_tokens")
public class RefreshTokenEntity {

  @Id
  @Column("token")
  private String token;

  @Column("user_id")
  private UUID userId;

  @Column("expires_at")
  private OffsetDateTime expiresAt;

  @Column("created_at")
  private OffsetDateTime createdAt;

}
