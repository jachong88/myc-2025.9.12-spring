package web.studio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import web.studio.entity.StudioEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudioRepository extends JpaRepository<StudioEntity, String> {

    /**
     * Find studio by ID with active status and not deleted
     */
    @Query("""
        SELECT s FROM StudioEntity s 
        WHERE s.id = :id 
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        """)
    Optional<StudioEntity> findActiveById(@Param("id") String id);

    /**
     * Find studios by owner ID with active status
     */
    @Query("""
        SELECT s FROM StudioEntity s 
        WHERE s.owner.id = :ownerId 
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        ORDER BY s.createdAt DESC
        """)
    List<StudioEntity> findActiveByOwnerId(@Param("ownerId") String ownerId);

    /**
     * Find studios by owner ID with pagination
     */
    @Query("""
        SELECT s FROM StudioEntity s 
        WHERE s.owner.id = :ownerId 
        AND s.deletedAt IS NULL
        ORDER BY s.createdAt DESC
        """)
    Page<StudioEntity> findByOwnerId(@Param("ownerId") String ownerId, Pageable pageable);

    /**
     * Check if email exists for active studios (for uniqueness validation)
     */
    @Query("""
        SELECT COUNT(s) > 0 FROM StudioEntity s 
        WHERE LOWER(s.email) = LOWER(:email) 
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        AND (:excludeId IS NULL OR s.id != :excludeId)
        """)
    boolean existsByEmailAndActive(@Param("email") String email, @Param("excludeId") String excludeId);

    /**
     * Check if studio code exists for active studios (for uniqueness validation)
     */
    @Query("""
        SELECT COUNT(s) > 0 FROM StudioEntity s 
        WHERE UPPER(s.code) = UPPER(:code) 
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        AND (:excludeId IS NULL OR s.id != :excludeId)
        """)
    boolean existsByCodeAndActive(@Param("code") String code, @Param("excludeId") String excludeId);

    /**
     * Search studios by name (full-text search)
     */
    @Query(value = """
        SELECT * FROM studio s
        WHERE to_tsvector('simple', s.name) @@ plainto_tsquery('simple', :searchTerm)
          AND s.status = 'ACTIVE'
          AND s.deleted_at IS NULL
        ORDER BY ts_rank_cd(to_tsvector('simple', s.name), plainto_tsquery('simple', :searchTerm)) DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM studio s
        WHERE to_tsvector('simple', s.name) @@ plainto_tsquery('simple', :searchTerm)
          AND s.status = 'ACTIVE'
          AND s.deleted_at IS NULL
        """,
        nativeQuery = true)
    Page<StudioEntity> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find studios in specific geographic area (by country and province)
     */
    @Query("""
        SELECT s FROM StudioEntity s
        JOIN s.address a
        WHERE a.countryCode = :countryCode
        AND (:provinceCode IS NULL OR a.provinceCode = :provinceCode)
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        ORDER BY s.name
        """)
    List<StudioEntity> findByGeographicArea(
            @Param("countryCode") String countryCode, 
            @Param("provinceCode") String provinceCode,
            Pageable pageable);

    /**
     * Find studios by postal code (exact match)
     */
    @Query("""
        SELECT s FROM StudioEntity s
        JOIN s.address a
        JOIN a.postalCodeReference pcr
        WHERE pcr.postalCode = :postalCode
        AND pcr.countryCode = :countryCode
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        ORDER BY s.name
        """)
    List<StudioEntity> findByPostalCode(
            @Param("postalCode") String postalCode, 
            @Param("countryCode") String countryCode);

    /**
     * Count active studios by owner
     */
    @Query("""
        SELECT COUNT(s) FROM StudioEntity s 
        WHERE s.owner.id = :ownerId 
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        """)
    long countActiveByOwnerId(@Param("ownerId") String ownerId);

    /**
     * Find all active studios with basic info (for dropdown lists)
     */
    @Query("""
        SELECT s.id, s.name, s.code FROM StudioEntity s 
        WHERE s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        ORDER BY s.name
        """)
    List<Object[]> findAllActiveBasicInfo();

    /**
     * Find studios with address details for reporting
     */
    @Query("""
        SELECT s, a, pcr FROM StudioEntity s
        JOIN FETCH s.address a
        JOIN FETCH a.postalCodeReference pcr
        WHERE s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        ORDER BY s.name
        """)
    List<StudioEntity> findAllWithAddressDetails();

    /**
     * Find studios by company name (partial match)
     */
    @Query("""
        SELECT s FROM StudioEntity s 
        WHERE LOWER(s.companyName) LIKE LOWER(CONCAT('%', :companyName, '%'))
        AND s.status = 'ACTIVE' 
        AND s.deletedAt IS NULL
        ORDER BY s.companyName
        """)
    List<StudioEntity> findByCompanyNameContaining(@Param("companyName") String companyName);

    /**
     * Find studios created within a date range (for audit/reporting)
     */
    @Query("""
        SELECT s FROM StudioEntity s 
        WHERE s.createdAt >= :startDate 
        AND s.createdAt <= :endDate
        AND s.deletedAt IS NULL
        ORDER BY s.createdAt DESC
        """)
    List<StudioEntity> findCreatedBetween(
            @Param("startDate") java.time.OffsetDateTime startDate,
            @Param("endDate") java.time.OffsetDateTime endDate);

    /**
     * Custom method names that Spring Data JPA can implement automatically
     */
    
    // Find by exact code (case-insensitive, active only)
    Optional<StudioEntity> findByCodeIgnoreCaseAndStatusAndDeletedAtIsNull(String code, StudioEntity.StudioStatus status);
    
    // Find by exact email (case-insensitive, active only)  
    Optional<StudioEntity> findByEmailIgnoreCaseAndStatusAndDeletedAtIsNull(String email, StudioEntity.StudioStatus status);
    
    // Check existence by name
    boolean existsByNameIgnoreCaseAndDeletedAtIsNull(String name);
    
    // Find all active studios
    List<StudioEntity> findByStatusAndDeletedAtIsNullOrderByNameAsc(StudioEntity.StudioStatus status);
}