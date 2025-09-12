package web.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import web.common.entity.BaseEntity;

@Entity
@Table(name = "user_scope")
public class UserScopeEntity extends BaseEntity {
  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "user_id", length = 26, nullable = false)
  private String userId;

  @Column(name = "scope_type", length = 50, nullable = false)
  private String scopeType;

  @Column(name = "scope_id", length = 26, nullable = false)
  private String scopeId;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getUserId() { return userId; }
  public void setUserId(String userId) { this.userId = userId; }

  public String getScopeType() { return scopeType; }
  public void setScopeType(String scopeType) { this.scopeType = scopeType; }

  public String getScopeId() { return scopeId; }
  public void setScopeId(String scopeId) { this.scopeId = scopeId; }
}
