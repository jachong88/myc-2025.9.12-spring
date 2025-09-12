package web.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import web.common.entity.BaseEntity;

@Entity
@Table(name = "user_roles")
public class UserRoleEntity extends BaseEntity {
  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "user_id", length = 26, nullable = false)
  private String userId;

  @Column(name = "role_id", length = 26, nullable = false)
  private String roleId;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }

  public String getRoleId() { return roleId; }
  public void setRoleId(String roleId) { this.roleId = roleId; }
}
