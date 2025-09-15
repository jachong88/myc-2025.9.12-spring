package web.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.location.entity.AddressEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, String> {

    /**
     * Find address by ID with active status and not deleted
     */
    @Query("""
        SELECT a FROM AddressEntity a 
        WHERE a.id = :id 
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        """)
    Optional<AddressEntity> findActiveById(@Param("id") String id);

    /**
     * Find addresses by postal code ID
     */
    @Query("""
        SELECT a FROM AddressEntity a 
        WHERE a.postalCodeReference.id = :postalCodeId 
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        ORDER BY a.createdAt DESC
        """)
    List<AddressEntity> findByPostalCodeReferenceId(@Param("postalCodeId") String postalCodeId);

    /**
     * Find addresses by country and province
     */
    @Query("""
        SELECT a FROM AddressEntity a 
        WHERE a.countryCode = :countryCode 
        AND (:provinceCode IS NULL OR a.provinceCode = :provinceCode)
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        ORDER BY a.city, a.streetLine1
        """)
    List<AddressEntity> findByCountryAndProvince(
            @Param("countryCode") String countryCode,
            @Param("provinceCode") String provinceCode);

    /**
     * Find addresses by postal code (exact match)
     */
    @Query("""
        SELECT a FROM AddressEntity a
        JOIN a.postalCodeReference pcr
        WHERE pcr.postalCode = :postalCode
        AND pcr.countryCode = :countryCode
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        ORDER BY a.streetLine1
        """)
    List<AddressEntity> findByPostalCode(
            @Param("postalCode") String postalCode,
            @Param("countryCode") String countryCode);

    /**
     * Search addresses by street name (partial match)
     */
    @Query("""
        SELECT a FROM AddressEntity a 
        WHERE (LOWER(a.streetLine1) LIKE LOWER(CONCAT('%', :streetName, '%'))
            OR LOWER(a.streetLine2) LIKE LOWER(CONCAT('%', :streetName, '%')))
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        ORDER BY a.streetLine1
        """)
    List<AddressEntity> findByStreetNameContaining(@Param("streetName") String streetName);

    /**
     * Find addresses with full details (including postal code and country info)
     */
    @Query("""
        SELECT a FROM AddressEntity a
        JOIN FETCH a.postalCodeReference pcr
        WHERE a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        ORDER BY a.countryCode, a.provinceCode, a.city, a.streetLine1
        """)
    List<AddressEntity> findAllWithPostalCodeDetails();

    /**
     * Count addresses by country
     */
    @Query("""
        SELECT COUNT(a) FROM AddressEntity a 
        WHERE a.countryCode = :countryCode 
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        """)
    long countByCountryCode(@Param("countryCode") String countryCode);

    /**
     * Find addresses in the same postal code area (for suggestions/similar addresses)
     */
    @Query("""
        SELECT a FROM AddressEntity a
        JOIN a.postalCodeReference pcr
        WHERE pcr.postalCode = :postalCode
        AND a.id != :excludeAddressId
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        ORDER BY a.streetLine1
        LIMIT 10
        """)
    List<AddressEntity> findSimilarAddresses(
            @Param("postalCode") String postalCode,
            @Param("excludeAddressId") String excludeAddressId);

    /**
     * Custom method names that Spring Data JPA can implement automatically
     */
    
    // Find all active addresses
    List<AddressEntity> findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc(AddressEntity.AddressStatus status);
    
    // Check if address exists with similar details (for duplicate detection)
    @Query("""
        SELECT COUNT(a) > 0 FROM AddressEntity a 
        WHERE LOWER(TRIM(a.streetLine1)) = LOWER(TRIM(:streetLine1))
        AND (:streetLine2 IS NULL OR LOWER(TRIM(a.streetLine2)) = LOWER(TRIM(:streetLine2)))
        AND a.postalCodeReference.id = :postalCodeId
        AND a.status = 'ACTIVE' 
        AND a.deletedAt IS NULL
        AND (:excludeId IS NULL OR a.id != :excludeId)
        """)
    boolean existsSimilarAddress(
            @Param("streetLine1") String streetLine1,
            @Param("streetLine2") String streetLine2,
            @Param("postalCodeId") String postalCodeId,
            @Param("excludeId") String excludeId);
}