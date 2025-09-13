package web.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;

@Entity
@Table(name = "postal_code_reference", indexes = {
    // Indexes are created in migration for performance optimization
    // Listed here for documentation purposes
    @Index(name = "ux_postal_country", columnList = "postal_code, country_code", unique = true),
    @Index(name = "ix_postal_autocomplete", columnList = "country_code, postal_code"),
    @Index(name = "ix_province_country", columnList = "province_code, country_code")
})
public class PostalCodeReferenceEntity extends web.common.entity.BaseEntity {
  
  @Id
  @Column(name = "id", length = 26, nullable = false)
  private String id;

  @Column(name = "postal_code", length = 16, nullable = false)
  private String postalCode;

  @Column(name = "city", length = 120)
  private String city;

  @Column(name = "province_code", length = 5, nullable = false)
  private String provinceCode;

  @Column(name = "country_code", length = 2, nullable = false)
  private String countryCode;

  @Column(name = "status", length = 20)
  private String status;

  // Default constructor
  public PostalCodeReferenceEntity() {}

  // Constructor with required fields
  public PostalCodeReferenceEntity(String id, String postalCode, String provinceCode, String countryCode) {
    this.id = id;
    this.postalCode = postalCode;
    this.provinceCode = provinceCode;
    this.countryCode = countryCode;
    this.status = "active";
  }

  // Constructor with all fields
  public PostalCodeReferenceEntity(String id, String postalCode, String city, 
                                 String provinceCode, String countryCode, String status) {
    this.id = id;
    this.postalCode = postalCode;
    this.city = city;
    this.provinceCode = provinceCode;
    this.countryCode = countryCode;
    this.status = status;
  }

  // Getters and Setters
  public String getId() { 
    return id; 
  }

  public void setId(String id) { 
    this.id = id; 
  }

  public String getPostalCode() { 
    return postalCode; 
  }

  public void setPostalCode(String postalCode) { 
    this.postalCode = postalCode; 
  }

  public String getCity() { 
    return city; 
  }

  public void setCity(String city) { 
    this.city = city; 
  }

  public String getProvinceCode() { 
    return provinceCode; 
  }

  public void setProvinceCode(String provinceCode) { 
    this.provinceCode = provinceCode; 
  }

  public String getCountryCode() { 
    return countryCode; 
  }

  public void setCountryCode(String countryCode) { 
    this.countryCode = countryCode; 
  }

  public String getStatus() { 
    return status; 
  }

  public void setStatus(String status) { 
    this.status = status; 
  }

  // toString for debugging
  @Override
  public String toString() {
    return "PostalCodeReferenceEntity{" +
        "id='" + id + '\'' +
        ", postalCode='" + postalCode + '\'' +
        ", city='" + city + '\'' +
        ", provinceCode='" + provinceCode + '\'' +
        ", countryCode='" + countryCode + '\'' +
        ", status='" + status + '\'' +
        '}';
  }

  // equals and hashCode based on business key (postal_code + country_code)
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    
    PostalCodeReferenceEntity that = (PostalCodeReferenceEntity) obj;
    
    if (postalCode != null ? !postalCode.equals(that.postalCode) : that.postalCode != null) 
      return false;
    return countryCode != null ? countryCode.equals(that.countryCode) : that.countryCode == null;
  }

  @Override
  public int hashCode() {
    int result = postalCode != null ? postalCode.hashCode() : 0;
    result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
    return result;
  }
}