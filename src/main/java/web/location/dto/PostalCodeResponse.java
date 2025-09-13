package web.location.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import web.location.entity.PostalCodeReferenceEntity;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostalCodeResponse(
    String id,
    String postalCode,
    String city,
    String provinceCode,
    String countryCode,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
  
  public static PostalCodeResponse fromEntity(PostalCodeReferenceEntity entity) {
    if (entity == null) {
      return null;
    }
    
    return new PostalCodeResponse(
        entity.getId(),
        entity.getPostalCode(),
        entity.getCity(),
        entity.getProvinceCode(),
        entity.getCountryCode(),
        entity.getStatus(),
        entity.getCreatedAt(),
        entity.getUpdatedAt()
    );
  }
}