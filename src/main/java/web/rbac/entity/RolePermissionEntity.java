package web.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import web.common.entity.BaseEntity;

@Entity
@Table(name = "role_permissions")
public class RolePermissionEntity extends BaseEntity {
  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "role_id", length = 26, nullable = false)
  private String roleId;

  @Column(name = "permission_id", length = 26, nullable = false)
  private String permissionId;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getRoleId() { return roleId; }
  public void setRoleId(String roleId) { this.roleId = roleId; }

  public String getPermissionId() { return permissionId; }
  public void setPermissionId(String permissionId) { this.permissionId = permissionId; }
}
