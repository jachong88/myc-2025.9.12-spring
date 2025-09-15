package web.location.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import web.common.entity.BaseEntity;

@Entity
@Table(name = "address")
public class AddressEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    @Pattern(regexp = "^[0-7][0-9A-HJKMNP-TV-Z]{25}$", message = "Invalid ULID format")
    private String id;

    // Street address
    @Column(name = "street_line1", nullable = false)
    @NotBlank(message = "Street line 1 is required")
    @Size(max = 255, message = "Street line 1 cannot exceed 255 characters")
    private String streetLine1;

    @Column(name = "street_line2")
    @Size(max = 255, message = "Street line 2 cannot exceed 255 characters")
    private String streetLine2;

    // City (nullable based on country requirements)
    @Column(name = "city", length = 120)
    @Size(max = 120, message = "City cannot exceed 120 characters")
    private String city;

    // Geographic references
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postal_code_id", nullable = false)
    private PostalCodeReferenceEntity postalCodeReference;

    @Column(name = "province_code", length = 5, nullable = false)
    @NotBlank(message = "Province code is required")
    @Pattern(regexp = "^[A-Z0-9]{1,5}$", message = "Invalid province code format")
    private String provinceCode;

    @Column(name = "country_code", length = 2, nullable = false)
    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be 2 uppercase letters")
    private String countryCode;

    // Optional contact information
    @Column(name = "attention", length = 120)
    @Size(max = 120, message = "Attention line cannot exceed 120 characters")
    private String attention;

    // Status
    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private AddressStatus status = AddressStatus.ACTIVE;

    // Constructors
    public AddressEntity() {}

    public AddressEntity(String id, String streetLine1, PostalCodeReferenceEntity postalCodeReference,
                        String provinceCode, String countryCode, String createdBy) {
        this.id = id;
        this.streetLine1 = streetLine1;
        this.postalCodeReference = postalCodeReference;
        this.provinceCode = provinceCode;
        this.countryCode = countryCode;
        setCreatedBy(createdBy);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStreetLine1() { return streetLine1; }
    public void setStreetLine1(String streetLine1) { this.streetLine1 = streetLine1; }

    public String getStreetLine2() { return streetLine2; }
    public void setStreetLine2(String streetLine2) { this.streetLine2 = streetLine2; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public PostalCodeReferenceEntity getPostalCodeReference() { return postalCodeReference; }
    public void setPostalCodeReference(PostalCodeReferenceEntity postalCodeReference) { this.postalCodeReference = postalCodeReference; }

    public String getProvinceCode() { return provinceCode; }
    public void setProvinceCode(String provinceCode) { this.provinceCode = provinceCode; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getAttention() { return attention; }
    public void setAttention(String attention) { this.attention = attention; }

    public AddressStatus getStatus() { return status; }
    public void setStatus(AddressStatus status) { this.status = status; }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        address.append(streetLine1);
        
        if (streetLine2 != null && !streetLine2.trim().isEmpty()) {
            address.append(", ").append(streetLine2);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            address.append(", ").append(city);
        }
        
        if (postalCodeReference != null) {
            address.append(" ").append(postalCodeReference.getPostalCode());
        }
        
        return address.toString();
    }

    @Override
    public String toString() {
        return "AddressEntity{" +
                "id='" + id + '\'' +
                ", streetLine1='" + streetLine1 + '\'' +
                ", city='" + city + '\'' +
                ", provinceCode='" + provinceCode + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", status=" + status +
                '}';
    }

    // Address Status Enum
    public enum AddressStatus {
        ACTIVE("active"),
        INACTIVE("inactive"),
        ARCHIVED("archived");

        private final String value;

        AddressStatus(String value) {
            this.value = value;
        }

        public String getValue() { return value; }

        @Override
        public String toString() { return value; }
    }
}
