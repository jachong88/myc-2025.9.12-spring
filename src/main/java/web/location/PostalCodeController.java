package web.location;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.common.dto.ApiResponse;
import web.common.request.RequestIdHolder;
import web.location.dto.PostalCodeResponse;
import web.location.dto.PostalCodeCreateRequest;
import web.location.dto.PostalCodeUpdateRequest;
import web.location.dto.PostalCodeValidationResponse;
import web.location.entity.PostalCodeReferenceEntity;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/postal-codes")
@Tag(name = "Postal Codes", description = "Postal code reference and validation API")
public class PostalCodeController {

  private final PostalCodeReferenceService postalCodeService;

  @Autowired
  public PostalCodeController(PostalCodeReferenceService postalCodeService) {
    this.postalCodeService = postalCodeService;
  }

  @GetMapping("/search")
  @Operation(summary = "Autocomplete postal codes", 
             description = "Search postal codes by prefix for autocomplete functionality")
  public ResponseEntity<ApiResponse<List<PostalCodeResponse>>> searchPostalCodes(
      @Parameter(description = "Country code (ISO 3166-1 alpha-2)", example = "CA", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 2) String countryCode,
      
      @Parameter(description = "Postal code prefix to search", example = "M4B", required = true)
      @RequestParam @NotBlank @Size(min = 1, max = 16) String query,
      
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      
      @Parameter(description = "Page size (max 50)", example = "20")
      @RequestParam(defaultValue = "20") int size) {

    Page<PostalCodeReferenceEntity> result = postalCodeService.searchPostalCodes(
        countryCode, query, page, size);
    
    List<PostalCodeResponse> data = result.getContent().stream()
        .map(PostalCodeResponse::fromEntity)
        .toList();
    
    String rid = RequestIdHolder.getOrCreate();
    Map<String, Object> meta = Map.of(
        "page", page,
        "size", size,
        "totalItems", result.getTotalElements(),
        "totalPages", result.getTotalPages(),
        "hasNext", result.hasNext()
    );
    
    return ResponseEntity.ok(ApiResponse.success(rid, data, meta));
  }

  @GetMapping("/validate")
  @Operation(summary = "Validate postal code", 
             description = "Validate if a postal code exists and get its details")
  public ResponseEntity<ApiResponse<PostalCodeValidationResponse>> validatePostalCode(
      @Parameter(description = "Country code (ISO 3166-1 alpha-2)", example = "CA", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 2) String countryCode,
      
      @Parameter(description = "Postal code to validate", example = "M4B 1A1", required = true)
      @RequestParam @NotBlank @Size(min = 3, max = 16) String postalCode) {

    Optional<PostalCodeReferenceEntity> entityOpt = postalCodeService.findByPostalCodeAndCountryCode(
        postalCode, countryCode);
    
    PostalCodeValidationResponse data;
    if (entityOpt.isPresent()) {
      data = new PostalCodeValidationResponse(true, PostalCodeResponse.fromEntity(entityOpt.get()));
    } else {
      // Check if format is valid even if not found in database
      boolean validFormat = postalCodeService.isValidPostalCodeFormat(postalCode, countryCode);
      data = new PostalCodeValidationResponse(false, null, validFormat ? "Valid format but not found in database" : "Invalid postal code format");
    }
    
    String rid = RequestIdHolder.getOrCreate();
    return ResponseEntity.ok(ApiResponse.success(rid, data, null));
  }

  @GetMapping("/cities")
  @Operation(summary = "Get cities for country", 
             description = "Get distinct cities available for a specific country")
  public ResponseEntity<ApiResponse<List<String>>> getCitiesByCountry(
      @Parameter(description = "Country code (ISO 3166-1 alpha-2)", example = "CA", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 2) String countryCode) {

    List<String> cities = postalCodeService.getDistinctCitiesByCountryCode(countryCode);
    
    String rid = RequestIdHolder.getOrCreate();
    Map<String, Object> meta = Map.of("count", cities.size());
    
    return ResponseEntity.ok(ApiResponse.success(rid, cities, meta));
  }

  @GetMapping("/provinces")
  @Operation(summary = "Get provinces for country", 
             description = "Get distinct province codes available for a specific country")
  public ResponseEntity<ApiResponse<List<String>>> getProvincesByCountry(
      @Parameter(description = "Country code (ISO 3166-1 alpha-2)", example = "CA", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 2) String countryCode) {

    List<String> provinces = postalCodeService.getDistinctProvinceCodesByCountryCode(countryCode);
    
    String rid = RequestIdHolder.getOrCreate();
    Map<String, Object> meta = Map.of("count", provinces.size());
    
    return ResponseEntity.ok(ApiResponse.success(rid, provinces, meta));
  }

  @GetMapping("/search/city")
  @Operation(summary = "Search postal codes by city", 
             description = "Search postal codes by city name")
  public ResponseEntity<ApiResponse<List<PostalCodeResponse>>> searchByCity(
      @Parameter(description = "Country code (ISO 3166-1 alpha-2)", example = "CA", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 2) String countryCode,
      
      @Parameter(description = "City name to search", example = "Toronto", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 100) String city,
      
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      
      @Parameter(description = "Page size (max 50)", example = "20")
      @RequestParam(defaultValue = "20") int size) {

    Page<PostalCodeReferenceEntity> result = postalCodeService.searchByCity(
        countryCode, city, page, size);
    
    List<PostalCodeResponse> data = result.getContent().stream()
        .map(PostalCodeResponse::fromEntity)
        .toList();
    
    String rid = RequestIdHolder.getOrCreate();
    Map<String, Object> meta = Map.of(
        "page", page,
        "size", size,
        "totalItems", result.getTotalElements(),
        "totalPages", result.getTotalPages(),
        "hasNext", result.hasNext()
    );
    
    return ResponseEntity.ok(ApiResponse.success(rid, data, meta));
  }

  @GetMapping("/search/province")
  @Operation(summary = "Search postal codes by province", 
             description = "Search postal codes by province code")
  public ResponseEntity<ApiResponse<List<PostalCodeResponse>>> searchByProvince(
      @Parameter(description = "Country code (ISO 3166-1 alpha-2)", example = "CA", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 2) String countryCode,
      
      @Parameter(description = "Province code", example = "ON", required = true)
      @RequestParam @NotBlank @Size(min = 2, max = 5) String provinceCode,
      
      @Parameter(description = "Page number (0-based)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      
      @Parameter(description = "Page size (max 100)", example = "20")
      @RequestParam(defaultValue = "20") int size) {

    Page<PostalCodeReferenceEntity> result = postalCodeService.findByProvinceAndCountry(
        provinceCode, countryCode, page, size);
    
    List<PostalCodeResponse> data = result.getContent().stream()
        .map(PostalCodeResponse::fromEntity)
        .toList();
    
    String rid = RequestIdHolder.getOrCreate();
    Map<String, Object> meta = Map.of(
        "page", page,
        "size", size,
        "totalItems", result.getTotalElements(),
        "totalPages", result.getTotalPages(),
        "hasNext", result.hasNext()
    );
    
    return ResponseEntity.ok(ApiResponse.success(rid, data, meta));
  }

  @GetMapping("/stats/{countryCode}")
  @Operation(summary = "Get postal code statistics", 
             description = "Get statistics for postal codes by country")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(
      @Parameter(description = "Country code (ISO 3166-1 alpha-2)", example = "CA", required = true)
      @PathVariable @Size(min = 2, max = 2) String countryCode) {

    Long totalCount = postalCodeService.countByCountryCode(countryCode);
    List<String> provinces = postalCodeService.getDistinctProvinceCodesByCountryCode(countryCode);
    List<String> cities = postalCodeService.getDistinctCitiesByCountryCode(countryCode);
    
    Map<String, Object> data = Map.of(
        "countryCode", countryCode.toUpperCase(),
        "totalPostalCodes", totalCount,
        "provinceCount", provinces.size(),
        "cityCount", cities.size()
    );
    
    String rid = RequestIdHolder.getOrCreate();
    return ResponseEntity.ok(ApiResponse.success(rid, data, null));
  }

  // Admin endpoints for managing postal codes
  
  @PostMapping
  @Operation(summary = "Create postal code reference", 
             description = "Create a new postal code reference entry")
  public ResponseEntity<ApiResponse<PostalCodeResponse>> createPostalCode(
      @Valid @RequestBody PostalCodeCreateRequest request) {

    // Get current user ID from security context (placeholder for actual implementation)
    String currentUserId = "system"; // TODO: Get from SecurityContext
    
    PostalCodeReferenceEntity created = postalCodeService.createPostalCodeReference(
        request.postalCode(), request.city(), request.provinceCode(), 
        request.countryCode(), currentUserId);
    
    PostalCodeResponse data = PostalCodeResponse.fromEntity(created);
    String rid = RequestIdHolder.getOrCreate();
    
    return ResponseEntity
        .created(URI.create("/api/v1/postal-codes/" + created.getId()))
        .body(ApiResponse.success(rid, data, null));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get postal code by ID", 
             description = "Get postal code reference by ID")
  public ResponseEntity<ApiResponse<PostalCodeResponse>> getById(
      @Parameter(description = "Postal code reference ID", required = true)
      @PathVariable String id) {

    Optional<PostalCodeReferenceEntity> entityOpt = postalCodeService.findById(id);
    if (entityOpt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    
    PostalCodeResponse data = PostalCodeResponse.fromEntity(entityOpt.get());
    String rid = RequestIdHolder.getOrCreate();
    
    return ResponseEntity.ok(ApiResponse.success(rid, data, null));
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update postal code reference", 
             description = "Update an existing postal code reference")
  public ResponseEntity<ApiResponse<PostalCodeResponse>> updatePostalCode(
      @Parameter(description = "Postal code reference ID", required = true)
      @PathVariable String id,
      @Valid @RequestBody PostalCodeUpdateRequest request) {

    // Get current user ID from security context (placeholder)
    String currentUserId = "system"; // TODO: Get from SecurityContext
    
    Optional<PostalCodeReferenceEntity> updated = postalCodeService.updatePostalCodeReference(
        id, request.city(), request.provinceCode(), request.status(), currentUserId);
    
    if (updated.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    
    PostalCodeResponse data = PostalCodeResponse.fromEntity(updated.get());
    String rid = RequestIdHolder.getOrCreate();
    
    return ResponseEntity.ok(ApiResponse.success(rid, data, null));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete postal code reference", 
             description = "Soft delete a postal code reference")
  public ResponseEntity<ApiResponse<Void>> deletePostalCode(
      @Parameter(description = "Postal code reference ID", required = true)
      @PathVariable String id) {

    // Get current user ID from security context (placeholder)
    String currentUserId = "system"; // TODO: Get from SecurityContext
    
    boolean deleted = postalCodeService.deletePostalCodeReference(id, currentUserId);
    if (!deleted) {
      return ResponseEntity.notFound().build();
    }
    
    String rid = RequestIdHolder.getOrCreate();
    return ResponseEntity.ok(ApiResponse.success(rid, null, null));
  }
}