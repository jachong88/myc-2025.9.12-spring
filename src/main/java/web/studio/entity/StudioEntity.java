package web.studio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import web.common.entity.BaseEntity;
import web.location.entity.AddressEntity;
import web.user.entity.UserEntity;

@Entity
@Table(name = "studio")
public class StudioEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 26, nullable = false)
    @Pattern(regexp = "^[0-7][0-9A-HJKMNP-TV-Z]{25}$", message = "Invalid ULID format")
    private String id;

    // Studio identification
    @Column(name = "name", nullable = false)
    @NotBlank(message = "Studio name is required")
    @Size(max = 255, message = "Studio name cannot exceed 255 characters")
    private String name;

    @Column(name = "code", length = 50, nullable = false)
    @NotBlank(message = "Studio code is required")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Studio code must contain only uppercase letters, numbers, underscore, and hyphens")
    @Size(max = 50, message = "Studio code cannot exceed 50 characters")
    private String code;

    // Contact information
    @Column(name = "phone", length = 20)
    @Pattern(regexp = "^[\\+]?[0-9\\s\\-\\(\\)]{7,20}$", message = "Invalid phone number format")
    private String phone;

    @Column(name = "email", length = 320)
    @Email(message = "Invalid email format")
    @Size(max = 320, message = "Email cannot exceed 320 characters")
    private String email;

    // Company information
    @Column(name = "company_name", nullable = false)
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name cannot exceed 255 characters")
    private String companyName;

    @Column(name = "company_registration_no", length = 100)
    @Size(max = 100, message = "Company registration number cannot exceed 100 characters")
    private String companyRegistrationNo;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    // Additional information
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Status
    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private StudioStatus status = StudioStatus.ACTIVE;

    // Constructors
    public StudioEntity() {}

    public StudioEntity(String id, String name, String code, String companyName, 
                       AddressEntity address, UserEntity owner, String createdBy) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.companyName = companyName;
        this.address = address;
        this.owner = owner;
        setCreatedBy(createdBy);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyRegistrationNo() { return companyRegistrationNo; }
    public void setCompanyRegistrationNo(String companyRegistrationNo) { this.companyRegistrationNo = companyRegistrationNo; }

    public AddressEntity getAddress() { return address; }
    public void setAddress(AddressEntity address) { this.address = address; }

    public UserEntity getOwner() { return owner; }
    public void setOwner(UserEntity owner) { this.owner = owner; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public StudioStatus getStatus() { return status; }
    public void setStatus(StudioStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "StudioEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", companyName='" + companyName + '\'' +
                ", status=" + status +
                '}';
    }

    // Studio Status Enum
    public enum StudioStatus {
        ACTIVE("active"),
        INACTIVE("inactive"),
        SUSPENDED("suspended"),
        ARCHIVED("archived");

        private final String value;

        StudioStatus(String value) {
            this.value = value;
        }

        public String getValue() { return value; }

        @Override
        public String toString() { return value; }
    }
}
