package web.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import web.location.entity.PostalCodeReferenceEntity;
import com.github.f4b6a3.ulid.UlidCreator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@Transactional
class PostalCodeReferenceRepositoryIntegrationTest {

  @Autowired
  private PostalCodeReferenceRepository repository;

  private PostalCodeReferenceEntity testEntity1;
  private PostalCodeReferenceEntity testEntity2;
  private PostalCodeReferenceEntity testEntity3;

  @BeforeEach
  void setUp() {
    repository.deleteAll();
    
    // Create test data for Canada
    testEntity1 = new PostalCodeReferenceEntity();
    testEntity1.setId(UlidCreator.getUlid().toString());
    testEntity1.setPostalCode("M4B 1A1");
    testEntity1.setCity("Toronto");
    testEntity1.setProvinceCode("ON");
    testEntity1.setCountryCode("CA");
    testEntity1.setStatus("active");
    testEntity1.setCreatedAt(Instant.now());
    testEntity1.setCreatedBy("test");
    testEntity1.setUpdatedAt(Instant.now());
    
    testEntity2 = new PostalCodeReferenceEntity();
    testEntity2.setId(UlidCreator.getUlid().toString());
    testEntity2.setPostalCode("M4B 2C3");
    testEntity2.setCity("Toronto");
    testEntity2.setProvinceCode("ON");
    testEntity2.setCountryCode("CA");
    testEntity2.setStatus("active");
    testEntity2.setCreatedAt(Instant.now());
    testEntity2.setCreatedBy("test");
    testEntity2.setUpdatedAt(Instant.now());
    
    // Create test data for Singapore
    testEntity3 = new PostalCodeReferenceEntity();
    testEntity3.setId(UlidCreator.getUlid().toString());
    testEntity3.setPostalCode("510123");
    testEntity3.setCity("Singapore");
    testEntity3.setProvinceCode("SG");
    testEntity3.setCountryCode("SG");
    testEntity3.setStatus("active");
    testEntity3.setCreatedAt(Instant.now());
    testEntity3.setCreatedBy("test");
    testEntity3.setUpdatedAt(Instant.now());
    
    repository.saveAll(List.of(testEntity1, testEntity2, testEntity3));
  }

  @Test
  void testFindByPostalCodeAndCountryCode_Found() {
    Optional<PostalCodeReferenceEntity> result = repository.findByPostalCodeAndCountryCode("M4B 1A1", "CA");
    
    assertThat(result).isPresent();
    assertThat(result.get().getPostalCode()).isEqualTo("M4B 1A1");
    assertThat(result.get().getCountryCode()).isEqualTo("CA");
    assertThat(result.get().getCity()).isEqualTo("Toronto");
  }

  @Test
  void testFindByPostalCodeAndCountryCode_NotFound() {
    Optional<PostalCodeReferenceEntity> result = repository.findByPostalCodeAndCountryCode("XXX XXX", "CA");
    
    assertThat(result).isNotPresent();
  }

  @Test
  void testFindByCountryCodeAndPostalCodeStartingWithIgnoreCase() {
    Page<PostalCodeReferenceEntity> result = repository.findByCountryCodeAndPostalCodeStartingWithIgnoreCase(
        "CA", "M4B%", PageRequest.of(0, 10));
    
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent().get(0).getPostalCode()).startsWith("M4B");
    assertThat(result.getContent().get(1).getPostalCode()).startsWith("M4B");
  }

  @Test
  void testFindByProvinceCodeAndCountryCode() {
    Page<PostalCodeReferenceEntity> result = repository.findByProvinceCodeAndCountryCode(
        "ON", "CA", PageRequest.of(0, 10));
    
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).allSatisfy(entity -> {
      assertThat(entity.getProvinceCode()).isEqualTo("ON");
      assertThat(entity.getCountryCode()).isEqualTo("CA");
    });
  }

  @Test
  void testFindByCountryCodeAndCityContainingIgnoreCase() {
    Page<PostalCodeReferenceEntity> result = repository.findByCountryCodeAndCityContainingIgnoreCase(
        "CA", "%Toronto%", PageRequest.of(0, 10));
    
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getContent()).allSatisfy(entity -> {
      assertThat(entity.getCity()).containsIgnoringCase("Toronto");
      assertThat(entity.getCountryCode()).isEqualTo("CA");
    });
  }

  @Test
  void testCountByCountryCode() {
    Long count = repository.countByCountryCode("CA");
    
    assertThat(count).isEqualTo(2L);
  }

  @Test
  void testCountByProvinceCodeAndCountryCode() {
    Long count = repository.countByProvinceCodeAndCountryCode("ON", "CA");
    
    assertThat(count).isEqualTo(2L);
  }

  @Test
  void testExistsByPostalCodeAndCountryCode_Exists() {
    boolean exists = repository.existsByPostalCodeAndCountryCode("M4B 1A1", "CA");
    
    assertThat(exists).isTrue();
  }

  @Test
  void testExistsByPostalCodeAndCountryCode_NotExists() {
    boolean exists = repository.existsByPostalCodeAndCountryCode("XXX XXX", "CA");
    
    assertThat(exists).isFalse();
  }

  @Test
  void testFindDistinctCitiesByCountryCode() {
    List<String> cities = repository.findDistinctCitiesByCountryCode("CA");
    
    assertThat(cities).containsExactly("Toronto");
  }

  @Test
  void testFindDistinctProvinceCodesByCountryCode() {
    List<String> provinces = repository.findDistinctProvinceCodesByCountryCode("CA");
    
    assertThat(provinces).containsExactly("ON");
  }

  @Test
  void testFindByPostalCodeInAndCountryCode() {
    List<String> postalCodes = List.of("M4B 1A1", "M4B 2C3", "XXX XXX");
    List<PostalCodeReferenceEntity> results = repository.findByPostalCodeInAndCountryCode(postalCodes, "CA");
    
    assertThat(results).hasSize(2);
    assertThat(results).allSatisfy(entity -> {
      assertThat(entity.getPostalCode()).isIn("M4B 1A1", "M4B 2C3");
      assertThat(entity.getCountryCode()).isEqualTo("CA");
    });
  }

  @Test
  void testSoftDelete_FilteredOut() {
    // Soft delete one entity
    testEntity1.setDeletedAt(Instant.now());
    testEntity1.setDeletedBy("test");
    testEntity1.setStatus("deleted");
    repository.save(testEntity1);
    
    // Should not find deleted entity
    Optional<PostalCodeReferenceEntity> result = repository.findByPostalCodeAndCountryCode("M4B 1A1", "CA");
    assertThat(result).isNotPresent();
    
    // Should still find active entity
    Optional<PostalCodeReferenceEntity> activeResult = repository.findByPostalCodeAndCountryCode("M4B 2C3", "CA");
    assertThat(activeResult).isPresent();
  }

  @Test
  void testInactiveStatus_FilteredOut() {
    // Set entity to inactive
    testEntity1.setStatus("inactive");
    repository.save(testEntity1);
    
    // Should not find inactive entity
    Optional<PostalCodeReferenceEntity> result = repository.findByPostalCodeAndCountryCode("M4B 1A1", "CA");
    assertThat(result).isNotPresent();
    
    // Should still find active entity
    Optional<PostalCodeReferenceEntity> activeResult = repository.findByPostalCodeAndCountryCode("M4B 2C3", "CA");
    assertThat(activeResult).isPresent();
  }
}