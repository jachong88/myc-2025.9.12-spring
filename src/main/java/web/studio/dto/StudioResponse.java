package web.studio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudioResponse {

    @JsonProperty("id")
    private String id;

    // Studio identification
    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    // Contact information
    @JsonProperty("phone")
    private String phone;

    @JsonProperty("email")
    private String email;

    // Company information
    @JsonProperty("companyName")
    private String companyName;

    @JsonProperty("companyRegistrationNo")
    private String companyRegistrationNo;

    // Additional information
    @JsonProperty("note")
    private String note;

    @JsonProperty("status")
    private String status;

    // Relationships
    @JsonProperty("owner")
    private OwnerResponse owner;

    @JsonProperty("address")
    private AddressResponse address;

    // Audit information
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    @JsonProperty("updatedAt")
    private OffsetDateTime updatedAt;

    // Constructors
    public StudioResponse() {}

    public StudioResponse(String id, String name, String code, String companyName, String status) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.companyName = companyName;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyRegistrationNo() {
        return companyRegistrationNo;
    }

    public void setCompanyRegistrationNo(String companyRegistrationNo) {
        this.companyRegistrationNo = companyRegistrationNo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OwnerResponse getOwner() {
        return owner;
    }

    public void setOwner(OwnerResponse owner) {
        this.owner = owner;
    }

    public AddressResponse getAddress() {
        return address;
    }

    public void setAddress(AddressResponse address) {
        this.address = address;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "StudioResponse{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", companyName='" + companyName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    // Nested DTO for owner information
    public static class OwnerResponse {
        @JsonProperty("id")
        private String id;

        @JsonProperty("fullName")
        private String fullName;

        @JsonProperty("email")
        private String email;

        @JsonProperty("phone")
        private String phone;

        // Constructors
        public OwnerResponse() {}

        public OwnerResponse(String id, String fullName, String email) {
            this.id = id;
            this.fullName = fullName;
            this.email = email;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        @Override
        public String toString() {
            return "OwnerResponse{" +
                    "id='" + id + '\'' +
                    ", fullName='" + fullName + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }

    // Nested DTO for address information
    public static class AddressResponse {
        @JsonProperty("id")
        private String id;

        @JsonProperty("streetLine1")
        private String streetLine1;

        @JsonProperty("streetLine2")
        private String streetLine2;

        @JsonProperty("city")
        private String city;

        @JsonProperty("attention")
        private String attention;

        @JsonProperty("postalCode")
        private PostalCodeResponse postalCode;

        @JsonProperty("fullAddress")
        private String fullAddress;

        // Constructors
        public AddressResponse() {}

        public AddressResponse(String id, String streetLine1, String city) {
            this.id = id;
            this.streetLine1 = streetLine1;
            this.city = city;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStreetLine1() {
            return streetLine1;
        }

        public void setStreetLine1(String streetLine1) {
            this.streetLine1 = streetLine1;
        }

        public String getStreetLine2() {
            return streetLine2;
        }

        public void setStreetLine2(String streetLine2) {
            this.streetLine2 = streetLine2;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getAttention() {
            return attention;
        }

        public void setAttention(String attention) {
            this.attention = attention;
        }

        public PostalCodeResponse getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(PostalCodeResponse postalCode) {
            this.postalCode = postalCode;
        }

        public String getFullAddress() {
            return fullAddress;
        }

        public void setFullAddress(String fullAddress) {
            this.fullAddress = fullAddress;
        }

        @Override
        public String toString() {
            return "AddressResponse{" +
                    "id='" + id + '\'' +
                    ", streetLine1='" + streetLine1 + '\'' +
                    ", city='" + city + '\'' +
                    ", fullAddress='" + fullAddress + '\'' +
                    '}';
        }
    }

    // Nested DTO for postal code information
    public static class PostalCodeResponse {
        @JsonProperty("id")
        private String id;

        @JsonProperty("postalCode")
        private String postalCode;

        @JsonProperty("city")
        private String city;

        @JsonProperty("provinceCode")
        private String provinceCode;

        @JsonProperty("provinceName")
        private String provinceName;

        @JsonProperty("countryCode")
        private String countryCode;

        @JsonProperty("countryName")
        private String countryName;

        // Constructors
        public PostalCodeResponse() {}

        public PostalCodeResponse(String id, String postalCode, String countryCode) {
            this.id = id;
            this.postalCode = postalCode;
            this.countryCode = countryCode;
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

        public String getProvinceName() {
            return provinceName;
        }

        public void setProvinceName(String provinceName) {
            this.provinceName = provinceName;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getCountryName() {
            return countryName;
        }

        public void setCountryName(String countryName) {
            this.countryName = countryName;
        }

        @Override
        public String toString() {
            return "PostalCodeResponse{" +
                    "id='" + id + '\'' +
                    ", postalCode='" + postalCode + '\'' +
                    ", city='" + city + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    '}';
        }
    }
}