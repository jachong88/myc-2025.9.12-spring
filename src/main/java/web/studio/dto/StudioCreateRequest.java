package web.studio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class StudioCreateRequest {

    // Studio identification
    @NotBlank(message = "Studio name is required")
    @Size(max = 255, message = "Studio name cannot exceed 255 characters")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Studio code is required")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Studio code must contain only uppercase letters, numbers, underscore, and hyphens")
    @Size(max = 50, message = "Studio code cannot exceed 50 characters")
    @JsonProperty("code")
    private String code;

    // Contact information
    @Pattern(regexp = "^[\\+]?[0-9\\s\\-\\(\\)]{7,20}$", message = "Invalid phone number format")
    @JsonProperty("phone")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 320, message = "Email cannot exceed 320 characters")
    @JsonProperty("email")
    private String email;

    // Company information
    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name cannot exceed 255 characters")
    @JsonProperty("companyName")
    private String companyName;

    @Size(max = 100, message = "Company registration number cannot exceed 100 characters")
    @JsonProperty("companyRegistrationNo")
    private String companyRegistrationNo;

    // Ownership
    @NotBlank(message = "Owner ID is required")
    @Pattern(regexp = "^[0-7][0-9A-HJKMNP-TV-Z]{25}$", message = "Invalid owner ID format")
    @JsonProperty("ownerId")
    private String ownerId;

    // Additional information
    @JsonProperty("note")
    private String note;

    // Address information (nested)
    @Valid
    @NotNull(message = "Address is required")
    @JsonProperty("address")
    private AddressCreateRequest address;

    // Constructors
    public StudioCreateRequest() {}

    public StudioCreateRequest(String name, String code, String companyName, String ownerId, AddressCreateRequest address) {
        this.name = name;
        this.code = code;
        this.companyName = companyName;
        this.ownerId = ownerId;
        this.address = address;
    }

    // Getters and Setters
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

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public AddressCreateRequest getAddress() {
        return address;
    }

    public void setAddress(AddressCreateRequest address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "StudioCreateRequest{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", companyName='" + companyName + '\'' +
                ", ownerId='" + ownerId + '\'' +
                '}';
    }

    // Nested DTO for address creation
    public static class AddressCreateRequest {

        @NotBlank(message = "Postal code ID is required")
        @Pattern(regexp = "^[0-7][0-9A-HJKMNP-TV-Z]{25}$", message = "Invalid postal code ID format")
        @JsonProperty("postalCodeId")
        private String postalCodeId;

        @NotBlank(message = "Street line 1 is required")
        @Size(max = 255, message = "Street line 1 cannot exceed 255 characters")
        @JsonProperty("streetLine1")
        private String streetLine1;

        @Size(max = 255, message = "Street line 2 cannot exceed 255 characters")
        @JsonProperty("streetLine2")
        private String streetLine2;

        @Size(max = 120, message = "City cannot exceed 120 characters")
        @JsonProperty("city")
        private String city;

        @Size(max = 120, message = "Attention line cannot exceed 120 characters")
        @JsonProperty("attention")
        private String attention;

        // Constructors
        public AddressCreateRequest() {}

        public AddressCreateRequest(String postalCodeId, String streetLine1) {
            this.postalCodeId = postalCodeId;
            this.streetLine1 = streetLine1;
        }

        // Getters and Setters
        public String getPostalCodeId() {
            return postalCodeId;
        }

        public void setPostalCodeId(String postalCodeId) {
            this.postalCodeId = postalCodeId;
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

        @Override
        public String toString() {
            return "AddressCreateRequest{" +
                    "postalCodeId='" + postalCodeId + '\'' +
                    ", streetLine1='" + streetLine1 + '\'' +
                    ", city='" + city + '\'' +
                    '}';
        }
    }
}