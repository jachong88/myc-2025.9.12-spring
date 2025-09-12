package web.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import web.common.entity.BaseEntity;

@Entity
@Table(name = "permissions")
public class PermissionEntity extends BaseEntity {
  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "resource", length = 50, nullable = false)
  private String resource;

  @Column(name = "action", length = 50, nullable = false)
  private String action;

  @Column(name = "scope", length = 50, nullable = false)
  private String scope;

  @Column(name = "effect", length = 10, nullable = false)
  private String effect;

  @Column(name = "qualifiers")
  private String qualifiers;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getResource() { return resource; }
  public void setResource(String resource) { this.resource = resource; }

  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }

  public String getScope() { return scope; }
  public void setScope(String scope) { this.scope = scope; }

  public String getEffect() { return effect; }
  public void setEffect(String effect) { this.effect = effect; }

  public String getQualifiers() { return qualifiers; }
  public void setQualifiers(String qualifiers) { this.qualifiers = qualifiers; }
}
