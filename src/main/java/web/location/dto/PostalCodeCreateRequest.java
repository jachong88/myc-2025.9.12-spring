package web.location.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostalCodeCreateRequest(
    @NotBlank(message = "Postal code is required")
    @Size(min = 3, max = 16, message = "Postal code must be between 3 and 16 characters")
    String postalCode,
    
    @Size(max = 120, message = "City name cannot exceed 120 characters")
    String city,
    
    @NotBlank(message = "Province code is required")
    @Size(min = 2, max = 5, message = "Province code must be between 2 and 5 characters")
    String provinceCode,
    
    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters")
    String countryCode
) {}