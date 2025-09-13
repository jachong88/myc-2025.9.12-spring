package web.location.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PostalCodeUpdateRequest(
    @Size(max = 120, message = "City name cannot exceed 120 characters")
    String city,
    
    @Size(min = 2, max = 5, message = "Province code must be between 2 and 5 characters")
    String provinceCode,
    
    @Pattern(regexp = "^(active|inactive|deprecated|deleted)$", 
             message = "Status must be one of: active, inactive, deprecated, deleted")
    String status
) {}