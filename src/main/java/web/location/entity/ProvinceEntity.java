package web.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "province")
public class ProvinceEntity extends web.common.entity.BaseEntity {
  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "country_code", length = 2, nullable = false)
  private String countryCode;

  @Column(name = "province_code", length = 10)
  private String provinceCode;

  @Column(name = "name", length = 100)
  private String name;

  @Column(name = "status", length = 20)
  private String status;

  public String getId() { return id; }
  public void setId(String id) { this.id = id; }

  public String getCountryCode() { return countryCode; }
  public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

  public String getProvinceCode() { return provinceCode; }
  public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
