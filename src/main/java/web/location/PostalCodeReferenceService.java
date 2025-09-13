package web.location;

import com.github.f4b6a3.ulid.UlidCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.location.entity.PostalCodeReferenceEntity;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class PostalCodeReferenceService {

  private final PostalCodeReferenceRepository postalCodeRepository;

  // Postal code validation patterns for common countries
  private static final Pattern CANADA_POSTAL_CODE = Pattern.compile("^[A-Z]\\d[A-Z] ?\\d[A-Z]\\d$");
  private static final Pattern US_ZIP_CODE = Pattern.compile("^\\d{5}(-\\d{4})?$");
  private static final Pattern UK_POSTAL_CODE = Pattern.compile("^[A-Z]{1,2}\\d[A-Z\\d]? ?\\d[A-Z]{2}$");
  private static final Pattern SINGAPORE_POSTAL_CODE = Pattern.compile("^\\d{6}$");
  private static final Pattern MALAYSIA_POSTAL_CODE = Pattern.compile("^\\d{5}$");

  @Autowired
  public PostalCodeReferenceService(PostalCodeReferenceRepository postalCodeRepository) {
    this.postalCodeRepository = postalCodeRepository;
  }

  /**
   * Find postal code by exact match for validation
   */
  public Optional<PostalCodeReferenceEntity> findByPostalCodeAndCountryCode(String postalCode, String countryCode) {
    if (postalCode == null || countryCode == null) {
      return Optional.empty();
    }
    
    String normalizedPostalCode = normalizePostalCode(postalCode, countryCode);
    String normalizedCountryCode = countryCode.toUpperCase().trim();
    
    return postalCodeRepository.findByPostalCodeAndCountryCode(normalizedPostalCode, normalizedCountryCode);
  }

  /**
   * Autocomplete search for postal codes
   */
  public Page<PostalCodeReferenceEntity> searchPostalCodes(String countryCode, String postalCodePrefix, int page, int size) {
    if (countryCode == null || postalCodePrefix == null) {
      return Page.empty();
    }
    
    String normalizedCountryCode = countryCode.toUpperCase().trim();
    String normalizedPrefix = normalizePostalCode(postalCodePrefix, countryCode) + "%";
    
    // Limit page size to prevent performance issues
    int safeSize = Math.min(size, 50);
    Pageable pageable = PageRequest.of(page, safeSize);
    
    return postalCodeRepository.findByCountryCodeAndPostalCodeStartingWithIgnoreCase(
        normalizedCountryCode, normalizedPrefix, pageable);
  }

  /**
   * Validate postal code format for a specific country
   */
  public boolean isValidPostalCodeFormat(String postalCode, String countryCode) {
    if (postalCode == null || countryCode == null) {
      return false;
    }
    
    String normalizedCode = postalCode.toUpperCase().trim();
    String normalizedCountry = countryCode.toUpperCase().trim();
    
    return switch (normalizedCountry) {
      case "CA" -> CANADA_POSTAL_CODE.matcher(normalizedCode).matches();
      case "US" -> US_ZIP_CODE.matcher(normalizedCode).matches();
      case "GB", "UK" -> UK_POSTAL_CODE.matcher(normalizedCode).matches();
      case "SG" -> SINGAPORE_POSTAL_CODE.matcher(normalizedCode).matches();
      case "MY" -> MALAYSIA_POSTAL_CODE.matcher(normalizedCode).matches();
      default -> normalizedCode.length() >= 3 && normalizedCode.length() <= 16; // Generic validation
    };
  }

  /**
   * Check if postal code exists in database
   */
  public boolean existsByPostalCodeAndCountryCode(String postalCode, String countryCode) {
    if (postalCode == null || countryCode == null) {
      return false;
    }
    
    String normalizedPostalCode = normalizePostalCode(postalCode, countryCode);
    String normalizedCountryCode = countryCode.toUpperCase().trim();
    
    return postalCodeRepository.existsByPostalCodeAndCountryCode(normalizedPostalCode, normalizedCountryCode);
  }

  /**
   * Validate multiple postal codes for batch operations
   */
  public List<PostalCodeReferenceEntity> validatePostalCodes(Collection<String> postalCodes, String countryCode) {
    if (postalCodes == null || postalCodes.isEmpty() || countryCode == null) {
      return List.of();
    }
    
    String normalizedCountryCode = countryCode.toUpperCase().trim();
    List<String> normalizedCodes = postalCodes.stream()
        .filter(code -> code != null && !code.trim().isEmpty())
        .map(code -> normalizePostalCode(code, countryCode))
        .toList();
    
    return postalCodeRepository.findByPostalCodeInAndCountryCode(normalizedCodes, normalizedCountryCode);
  }

  /**
   * Find postal codes by province and country for reporting
   */
  public Page<PostalCodeReferenceEntity> findByProvinceAndCountry(String provinceCode, String countryCode, int page, int size) {
    if (provinceCode == null || countryCode == null) {
      return Page.empty();
    }
    
    String normalizedProvinceCode = provinceCode.toUpperCase().trim();
    String normalizedCountryCode = countryCode.toUpperCase().trim();
    
    Pageable pageable = PageRequest.of(page, Math.min(size, 100));
    return postalCodeRepository.findByProvinceCodeAndCountryCode(normalizedProvinceCode, normalizedCountryCode, pageable);
  }

  /**
   * Search postal codes by city
   */
  public Page<PostalCodeReferenceEntity> searchByCity(String countryCode, String cityPattern, int page, int size) {
    if (countryCode == null || cityPattern == null) {
      return Page.empty();
    }
    
    String normalizedCountryCode = countryCode.toUpperCase().trim();
    String normalizedCityPattern = "%" + cityPattern.trim() + "%";
    
    Pageable pageable = PageRequest.of(page, Math.min(size, 50));
    return postalCodeRepository.findByCountryCodeAndCityContainingIgnoreCase(normalizedCountryCode, normalizedCityPattern, pageable);
  }

  /**
   * Get statistics for postal codes by country
   */
  public Long countByCountryCode(String countryCode) {
    if (countryCode == null) {
      return 0L;
    }
    return postalCodeRepository.countByCountryCode(countryCode.toUpperCase().trim());
  }

  /**
   * Get statistics for postal codes by province
   */
  public Long countByProvinceAndCountry(String provinceCode, String countryCode) {
    if (provinceCode == null || countryCode == null) {
      return 0L;
    }
    return postalCodeRepository.countByProvinceCodeAndCountryCode(
        provinceCode.toUpperCase().trim(), 
        countryCode.toUpperCase().trim());
  }

  /**
   * Get distinct cities for a country
   */
  public List<String> getDistinctCitiesByCountryCode(String countryCode) {
    if (countryCode == null) {
      return List.of();
    }
    return postalCodeRepository.findDistinctCitiesByCountryCode(countryCode.toUpperCase().trim());
  }

  /**
   * Get distinct province codes for a country
   */
  public List<String> getDistinctProvinceCodesByCountryCode(String countryCode) {
    if (countryCode == null) {
      return List.of();
    }
    return postalCodeRepository.findDistinctProvinceCodesByCountryCode(countryCode.toUpperCase().trim());
  }

  /**
   * Find postal code reference by ID
   */
  public Optional<PostalCodeReferenceEntity> findById(String id) {
    if (id == null) {
      return Optional.empty();
    }
    return postalCodeRepository.findById(id);
  }

  /**
   * Create a new postal code reference entry
   */
  @Transactional
  public PostalCodeReferenceEntity createPostalCodeReference(String postalCode, String city, 
                                                            String provinceCode, String countryCode, 
                                                            String createdBy) {
    if (postalCode == null || provinceCode == null || countryCode == null || createdBy == null) {
      throw new IllegalArgumentException("Required fields cannot be null");
    }

    // Validate format first
    if (!isValidPostalCodeFormat(postalCode, countryCode)) {
      throw new IllegalArgumentException("Invalid postal code format for country: " + countryCode);
    }

    String normalizedPostalCode = normalizePostalCode(postalCode, countryCode);
    String normalizedCountryCode = countryCode.toUpperCase().trim();
    String normalizedProvinceCode = provinceCode.toUpperCase().trim();

    // Check if already exists
    if (existsByPostalCodeAndCountryCode(normalizedPostalCode, normalizedCountryCode)) {
      throw new IllegalArgumentException("Postal code already exists: " + normalizedPostalCode + " in " + normalizedCountryCode);
    }

    PostalCodeReferenceEntity entity = new PostalCodeReferenceEntity();
    entity.setId(UlidCreator.getUlid().toString());
    entity.setPostalCode(normalizedPostalCode);
    entity.setCity(city != null ? city.trim() : null);
    entity.setProvinceCode(normalizedProvinceCode);
    entity.setCountryCode(normalizedCountryCode);
    entity.setStatus("active");
    entity.setCreatedBy(createdBy);
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());

    return postalCodeRepository.save(entity);
  }

  /**
   * Update an existing postal code reference entry
   */
  @Transactional
  public Optional<PostalCodeReferenceEntity> updatePostalCodeReference(String id, String city, 
                                                                       String provinceCode, String status,
                                                                       String updatedBy) {
    Optional<PostalCodeReferenceEntity> entityOpt = postalCodeRepository.findById(id);
    if (entityOpt.isEmpty()) {
      return Optional.empty();
    }

    PostalCodeReferenceEntity entity = entityOpt.get();
    if (city != null) {
      entity.setCity(city.trim());
    }
    if (provinceCode != null) {
      entity.setProvinceCode(provinceCode.toUpperCase().trim());
    }
    if (status != null) {
      entity.setStatus(status.toLowerCase().trim());
    }
    if (updatedBy != null) {
      entity.setUpdatedBy(updatedBy);
    }
    entity.setUpdatedAt(Instant.now());

    return Optional.of(postalCodeRepository.save(entity));
  }

  /**
   * Soft delete a postal code reference entry
   */
  @Transactional
  public boolean deletePostalCodeReference(String id, String deletedBy) {
    Optional<PostalCodeReferenceEntity> entityOpt = postalCodeRepository.findById(id);
    if (entityOpt.isEmpty()) {
      return false;
    }

    PostalCodeReferenceEntity entity = entityOpt.get();
    entity.setDeletedAt(Instant.now());
    entity.setDeletedBy(deletedBy);
    entity.setStatus("deleted");

    postalCodeRepository.save(entity);
    return true;
  }

  /**
   * Normalize postal code format based on country conventions
   */
  private String normalizePostalCode(String postalCode, String countryCode) {
    if (postalCode == null) {
      return null;
    }
    
    String normalized = postalCode.toUpperCase().trim();
    String country = countryCode.toUpperCase().trim();
    
    return switch (country) {
      case "CA" -> {
        // Canadian postal codes: remove spaces, ensure format A1A 1A1
        normalized = normalized.replaceAll("\\s+", "");
        if (normalized.length() == 6) {
          yield normalized.substring(0, 3) + " " + normalized.substring(3);
        }
        yield normalized;
      }
      case "US" -> {
        // US ZIP codes: remove spaces and hyphens for storage
        yield normalized.replaceAll("[\\s-]+", "");
      }
      case "GB", "UK" -> {
        // UK postal codes: normalize spacing
        normalized = normalized.replaceAll("\\s+", "");
        if (normalized.length() >= 5) {
          yield normalized.substring(0, normalized.length() - 3) + " " + normalized.substring(normalized.length() - 3);
        }
        yield normalized;
      }
      default -> normalized.replaceAll("\\s+", ""); // Remove spaces for other countries
    };
  }
}