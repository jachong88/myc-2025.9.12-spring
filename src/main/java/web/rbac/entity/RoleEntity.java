package web.rbac.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import web.common.entity.BaseEntity;

@Entity
@Table(name = "roles")
public class RoleEntity extends BaseEntity {
  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "name", length = 50, nullable = false, unique = true)
  private String name;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
}
