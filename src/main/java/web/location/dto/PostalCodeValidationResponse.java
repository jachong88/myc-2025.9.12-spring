package web.location.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostalCodeValidationResponse(
    boolean valid,
    PostalCodeResponse postalCode,
    String message
) {
  
  // Constructor for valid postal codes
  public PostalCodeValidationResponse(boolean valid, PostalCodeResponse postalCode) {
    this(valid, postalCode, null);
  }
  
  // Constructor for invalid postal codes with message
  public PostalCodeValidationResponse(boolean valid, PostalCodeResponse postalCode, String message) {
    this.valid = valid;
    this.postalCode = postalCode;
    this.message = message;
  }
}