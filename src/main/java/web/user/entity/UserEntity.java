package web.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import web.common.entity.BaseEntity;

@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "email", length = 320)
  private String email;

  @Column(name = "phone", length = 20)
  private String phone;

  @Column(name = "full_name", length = 255)
  private String fullName;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Column(name = "country_id", length = 26)
  private String countryId;

  @Column(name = "province_id", length = 26)
  private String provinceId;

  @Column(name = "role_id", length = 26)
  private String roleId;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }

  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }

  public boolean isActive() { return isActive; }
  public void setIsActive(boolean active) { isActive = active; }

  public String getCountryId() { return countryId; }
  public void setCountryId(String countryId) { this.countryId = countryId; }

  public String getProvinceId() { return provinceId; }
  public void setProvinceId(String provinceId) { this.provinceId = provinceId; }

  public String getRoleId() { return roleId; }
  public void setRoleId(String roleId) { this.roleId = roleId; }
}
