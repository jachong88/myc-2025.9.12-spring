package web.location;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import web.location.entity.PostalCodeReferenceEntity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostalCodeReferenceRepository extends JpaRepository<PostalCodeReferenceEntity, String> {

  // Exact lookup for validation
  @Query("""
      SELECT p FROM PostalCodeReferenceEntity p 
      WHERE p.postalCode = :postalCode 
        AND p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      """)
  Optional<PostalCodeReferenceEntity> findByPostalCodeAndCountryCode(
      @Param("postalCode") String postalCode, 
      @Param("countryCode") String countryCode);

  // Autocomplete search with prefix matching (case-insensitive)
  @Query("""
      SELECT p FROM PostalCodeReferenceEntity p 
      WHERE p.countryCode = :countryCode 
        AND UPPER(p.postalCode) LIKE UPPER(:postalCodePrefix) 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      ORDER BY p.postalCode ASC
      """)
  Page<PostalCodeReferenceEntity> findByCountryCodeAndPostalCodeStartingWithIgnoreCase(
      @Param("countryCode") String countryCode,
      @Param("postalCodePrefix") String postalCodePrefix,
      Pageable pageable);

  // Find all active postal codes for a specific country (for bulk operations)
  @Query("""
      SELECT p FROM PostalCodeReferenceEntity p 
      WHERE p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      ORDER BY p.postalCode ASC
      """)
  List<PostalCodeReferenceEntity> findByCountryCodeOrderByPostalCode(@Param("countryCode") String countryCode);

  // Find by multiple postal codes (batch validation)
  @Query("""
      SELECT p FROM PostalCodeReferenceEntity p 
      WHERE p.postalCode IN :postalCodes 
        AND p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      """)
  List<PostalCodeReferenceEntity> findByPostalCodeInAndCountryCode(
      @Param("postalCodes") Collection<String> postalCodes,
      @Param("countryCode") String countryCode);

  // Reporting: Find by province and country
  @Query("""
      SELECT p FROM PostalCodeReferenceEntity p 
      WHERE p.provinceCode = :provinceCode 
        AND p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      ORDER BY p.postalCode ASC
      """)
  Page<PostalCodeReferenceEntity> findByProvinceCodeAndCountryCode(
      @Param("provinceCode") String provinceCode,
      @Param("countryCode") String countryCode,
      Pageable pageable);

  // Find by city (case-insensitive, for city-based lookups)
  @Query("""
      SELECT p FROM PostalCodeReferenceEntity p 
      WHERE p.countryCode = :countryCode 
        AND UPPER(p.city) LIKE UPPER(:cityPattern) 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      ORDER BY p.city ASC, p.postalCode ASC
      """)
  Page<PostalCodeReferenceEntity> findByCountryCodeAndCityContainingIgnoreCase(
      @Param("countryCode") String countryCode,
      @Param("cityPattern") String cityPattern,
      Pageable pageable);

  // Count active postal codes by country (for statistics)
  @Query("""
      SELECT COUNT(p) FROM PostalCodeReferenceEntity p 
      WHERE p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      """)
  Long countByCountryCode(@Param("countryCode") String countryCode);

  // Count active postal codes by province (for statistics)
  @Query("""
      SELECT COUNT(p) FROM PostalCodeReferenceEntity p 
      WHERE p.provinceCode = :provinceCode 
        AND p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      """)
  Long countByProvinceCodeAndCountryCode(
      @Param("provinceCode") String provinceCode,
      @Param("countryCode") String countryCode);

  // Check if postal code exists (for faster existence checks)
  @Query("""
      SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END 
      FROM PostalCodeReferenceEntity p 
      WHERE p.postalCode = :postalCode 
        AND p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      """)
  boolean existsByPostalCodeAndCountryCode(
      @Param("postalCode") String postalCode, 
      @Param("countryCode") String countryCode);

  // Find distinct cities for a country (useful for dropdowns)
  @Query("""
      SELECT DISTINCT p.city FROM PostalCodeReferenceEntity p 
      WHERE p.countryCode = :countryCode 
        AND p.city IS NOT NULL 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      ORDER BY p.city ASC
      """)
  List<String> findDistinctCitiesByCountryCode(@Param("countryCode") String countryCode);

  // Find distinct province codes for a country (useful for dropdowns)
  @Query("""
      SELECT DISTINCT p.provinceCode FROM PostalCodeReferenceEntity p 
      WHERE p.countryCode = :countryCode 
        AND p.status = 'active' 
        AND p.deletedAt IS NULL
      ORDER BY p.provinceCode ASC
      """)
  List<String> findDistinctProvinceCodesByCountryCode(@Param("countryCode") String countryCode);
}