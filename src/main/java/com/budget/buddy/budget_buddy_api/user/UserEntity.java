package com.budget.buddy.budget_buddy_api.user;

import com.budget.buddy.budget_buddy_api.base.crudl.AuditableEntity;
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
 * User entity representing a user account in the system. Uses Spring Data JDBC for data access.
 */
@Table("users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class UserEntity extends AuditableEntity<UUID> {

  @ToString.Include
  @Column("username")
  private String username;

  @Column("password")
  private String password;

  @ToString.Include
  @Column("enabled")
  private boolean enabled;

}
