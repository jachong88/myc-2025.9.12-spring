package web.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import web.location.entity.PostalCodeReferenceEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostalCodeReferenceServiceTest {

  @Mock
  private PostalCodeReferenceRepository repository;

  @InjectMocks
  private PostalCodeReferenceService service;

  private PostalCodeReferenceEntity testEntity;

  @BeforeEach
  void setUp() {
    testEntity = new PostalCodeReferenceEntity();
    testEntity.setId("test-id");
    testEntity.setPostalCode("M4B 1A1");
    testEntity.setCity("Toronto");
    testEntity.setProvinceCode("ON");
    testEntity.setCountryCode("CA");
    testEntity.setStatus("active");
    testEntity.setCreatedAt(Instant.now());
    testEntity.setCreatedBy("test");
  }

  @Test
  void testFindByPostalCodeAndCountryCode_Found() {
    when(repository.findByPostalCodeAndCountryCode("M4B 1A1", "CA"))
        .thenReturn(Optional.of(testEntity));

    Optional<PostalCodeReferenceEntity> result = service.findByPostalCodeAndCountryCode("M4B 1A1", "CA");

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testEntity);
  }

  @Test
  void testFindByPostalCodeAndCountryCode_NotFound() {
    when(repository.findByPostalCodeAndCountryCode("XXX XXX", "CA"))
        .thenReturn(Optional.empty());

    Optional<PostalCodeReferenceEntity> result = service.findByPostalCodeAndCountryCode("XXX XXX", "CA");

    assertThat(result).isEmpty();
  }

  @Test
  void testFindByPostalCodeAndCountryCode_NullInputs() {
    Optional<PostalCodeReferenceEntity> result1 = service.findByPostalCodeAndCountryCode(null, "CA");
    Optional<PostalCodeReferenceEntity> result2 = service.findByPostalCodeAndCountryCode("M4B 1A1", null);

    assertThat(result1).isEmpty();
    assertThat(result2).isEmpty();
    verify(repository, never()).findByPostalCodeAndCountryCode(any(), any());
  }

  @Test
  void testSearchPostalCodes_Success() {
    List<PostalCodeReferenceEntity> entities = List.of(testEntity);
    Page<PostalCodeReferenceEntity> page = new PageImpl<>(entities, PageRequest.of(0, 20), 1);
    
    when(repository.findByCountryCodeAndPostalCodeStartingWithIgnoreCase(
        eq("CA"), eq("M4B%"), any(PageRequest.class)))
        .thenReturn(page);

    Page<PostalCodeReferenceEntity> result = service.searchPostalCodes("CA", "M4B", 0, 20);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0)).isEqualTo(testEntity);
  }

  @Test
  void testSearchPostalCodes_LimitPageSize() {
    service.searchPostalCodes("CA", "M4B", 0, 100); // Request size > 50

    verify(repository).findByCountryCodeAndPostalCodeStartingWithIgnoreCase(
        eq("CA"), eq("M4B%"), eq(PageRequest.of(0, 50)));
  }

  @Test
  void testSearchPostalCodes_NullInputs() {
    Page<PostalCodeReferenceEntity> result1 = service.searchPostalCodes(null, "M4B", 0, 20);
    Page<PostalCodeReferenceEntity> result2 = service.searchPostalCodes("CA", null, 0, 20);

    assertThat(result1).isEmpty();
    assertThat(result2).isEmpty();
    verify(repository, never()).findByCountryCodeAndPostalCodeStartingWithIgnoreCase(any(), any(), any());
  }

  @Test
  void testIsValidPostalCodeFormat_Canada() {
    assertThat(service.isValidPostalCodeFormat("M4B 1A1", "CA")).isTrue();
    assertThat(service.isValidPostalCodeFormat("M4B1A1", "CA")).isTrue(); // Should handle no space
    assertThat(service.isValidPostalCodeFormat("m4b 1a1", "CA")).isTrue(); // Should handle lowercase
    assertThat(service.isValidPostalCodeFormat("INVALID", "CA")).isFalse();
    assertThat(service.isValidPostalCodeFormat("12345", "CA")).isFalse();
  }

  @Test
  void testIsValidPostalCodeFormat_US() {
    assertThat(service.isValidPostalCodeFormat("12345", "US")).isTrue();
    assertThat(service.isValidPostalCodeFormat("12345-6789", "US")).isTrue();
    assertThat(service.isValidPostalCodeFormat("M4B 1A1", "US")).isFalse();
    assertThat(service.isValidPostalCodeFormat("INVALID", "US")).isFalse();
  }

  @Test
  void testIsValidPostalCodeFormat_Singapore() {
    assertThat(service.isValidPostalCodeFormat("510123", "SG")).isTrue();
    assertThat(service.isValidPostalCodeFormat("12345", "SG")).isFalse(); // Wrong length
    assertThat(service.isValidPostalCodeFormat("INVALID", "SG")).isFalse();
  }

  @Test
  void testIsValidPostalCodeFormat_Generic() {
    // Unknown country should use generic validation
    assertThat(service.isValidPostalCodeFormat("12345", "XX")).isTrue();
    assertThat(service.isValidPostalCodeFormat("ABCDE", "XX")).isTrue();
    assertThat(service.isValidPostalCodeFormat("AB", "XX")).isFalse(); // Too short
    assertThat(service.isValidPostalCodeFormat("A".repeat(20), "XX")).isFalse(); // Too long
  }

  @Test
  void testIsValidPostalCodeFormat_NullInputs() {
    assertThat(service.isValidPostalCodeFormat(null, "CA")).isFalse();
    assertThat(service.isValidPostalCodeFormat("M4B 1A1", null)).isFalse();
  }

  @Test
  void testExistsByPostalCodeAndCountryCode() {
    when(repository.existsByPostalCodeAndCountryCode("M4B 1A1", "CA")).thenReturn(true);
    when(repository.existsByPostalCodeAndCountryCode("XXX XXX", "CA")).thenReturn(false);

    assertThat(service.existsByPostalCodeAndCountryCode("M4B 1A1", "CA")).isTrue();
    assertThat(service.existsByPostalCodeAndCountryCode("XXX XXX", "CA")).isFalse();
  }

  @Test
  void testValidatePostalCodes() {
    List<String> inputCodes = List.of("M4B 1A1", "M4B 2C3", "INVALID");
    List<PostalCodeReferenceEntity> foundEntities = List.of(testEntity);

    when(repository.findByPostalCodeInAndCountryCode(anyList(), eq("CA")))
        .thenReturn(foundEntities);

    List<PostalCodeReferenceEntity> result = service.validatePostalCodes(inputCodes, "CA");

    assertThat(result).hasSize(1);
    assertThat(result.get(0)).isEqualTo(testEntity);
  }

  @Test
  void testCreatePostalCodeReference_Success() {
    when(repository.existsByPostalCodeAndCountryCode("M5V 1A1", "CA")).thenReturn(false);
    when(repository.save(any(PostalCodeReferenceEntity.class))).thenReturn(testEntity);

    PostalCodeReferenceEntity result = service.createPostalCodeReference(
        "M5V 1A1", "Toronto", "ON", "CA", "test-user");

    assertThat(result).isEqualTo(testEntity);
    verify(repository).save(any(PostalCodeReferenceEntity.class));
  }

  @Test
  void testCreatePostalCodeReference_NullRequiredFields() {
    assertThatThrownBy(() -> service.createPostalCodeReference(null, "Toronto", "ON", "CA", "test-user"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Required fields cannot be null");

    assertThatThrownBy(() -> service.createPostalCodeReference("M5V 1A1", "Toronto", null, "CA", "test-user"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Required fields cannot be null");
  }

  @Test
  void testCreatePostalCodeReference_InvalidFormat() {
    assertThatThrownBy(() -> service.createPostalCodeReference("INVALID", "Toronto", "ON", "CA", "test-user"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid postal code format");
  }

  @Test
  void testCreatePostalCodeReference_AlreadyExists() {
    when(repository.existsByPostalCodeAndCountryCode("M4B 1A1", "CA")).thenReturn(true);

    assertThatThrownBy(() -> service.createPostalCodeReference("M4B 1A1", "Toronto", "ON", "CA", "test-user"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Postal code already exists");
  }

  @Test
  void testUpdatePostalCodeReference_Success() {
    when(repository.findById("test-id")).thenReturn(Optional.of(testEntity));
    when(repository.save(any(PostalCodeReferenceEntity.class))).thenReturn(testEntity);

    Optional<PostalCodeReferenceEntity> result = service.updatePostalCodeReference(
        "test-id", "Updated City", "BC", "inactive", "test-user");

    assertThat(result).isPresent();
    verify(repository).save(any(PostalCodeReferenceEntity.class));
  }

  @Test
  void testUpdatePostalCodeReference_NotFound() {
    when(repository.findById("non-existent")).thenReturn(Optional.empty());

    Optional<PostalCodeReferenceEntity> result = service.updatePostalCodeReference(
        "non-existent", "Updated City", "BC", "inactive", "test-user");

    assertThat(result).isEmpty();
    verify(repository, never()).save(any());
  }

  @Test
  void testDeletePostalCodeReference_Success() {
    when(repository.findById("test-id")).thenReturn(Optional.of(testEntity));
    when(repository.save(any(PostalCodeReferenceEntity.class))).thenReturn(testEntity);

    boolean result = service.deletePostalCodeReference("test-id", "test-user");

    assertThat(result).isTrue();
    verify(repository).save(any(PostalCodeReferenceEntity.class));
  }

  @Test
  void testDeletePostalCodeReference_NotFound() {
    when(repository.findById("non-existent")).thenReturn(Optional.empty());

    boolean result = service.deletePostalCodeReference("non-existent", "test-user");

    assertThat(result).isFalse();
    verify(repository, never()).save(any());
  }

  @Test
  void testCountByCountryCode() {
    when(repository.countByCountryCode("CA")).thenReturn(100L);

    Long result = service.countByCountryCode("CA");

    assertThat(result).isEqualTo(100L);
  }

  @Test
  void testCountByCountryCode_NullInput() {
    Long result = service.countByCountryCode(null);

    assertThat(result).isEqualTo(0L);
    verify(repository, never()).countByCountryCode(any());
  }

  @Test
  void testNormalizePostalCode_Canada() {
    // Test private method indirectly through public methods
    when(repository.findByPostalCodeAndCountryCode("M4B 1A1", "CA"))
        .thenReturn(Optional.of(testEntity));

    // Should normalize M4B1A1 to M4B 1A1
    service.findByPostalCodeAndCountryCode("M4B1A1", "CA");

    verify(repository).findByPostalCodeAndCountryCode("M4B 1A1", "CA");
  }

  @Test
  void testNormalizePostalCode_US() {
    when(repository.findByPostalCodeAndCountryCode("123456789", "US"))
        .thenReturn(Optional.empty());

    // Should normalize 12345-6789 to 123456789 (remove hyphens)
    service.findByPostalCodeAndCountryCode("12345-6789", "US");

    verify(repository).findByPostalCodeAndCountryCode("123456789", "US");
  }

  @Test
  void testFindById_Success() {
    when(repository.findById("test-id")).thenReturn(Optional.of(testEntity));

    Optional<PostalCodeReferenceEntity> result = service.findById("test-id");

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(testEntity);
  }

  @Test
  void testFindById_NotFound() {
    when(repository.findById("non-existent")).thenReturn(Optional.empty());

    Optional<PostalCodeReferenceEntity> result = service.findById("non-existent");

    assertThat(result).isEmpty();
  }

  @Test
  void testFindById_NullInput() {
    Optional<PostalCodeReferenceEntity> result = service.findById(null);

    assertThat(result).isEmpty();
    verify(repository, never()).findById(any());
  }

  @Test
  void testGetDistinctCitiesByCountryCode() {
    List<String> cities = List.of("Toronto", "Ottawa", "Vancouver");
    when(repository.findDistinctCitiesByCountryCode("CA")).thenReturn(cities);

    List<String> result = service.getDistinctCitiesByCountryCode("CA");

    assertThat(result).isEqualTo(cities);
  }

  @Test
  void testGetDistinctProvinceCodesByCountryCode() {
    List<String> provinces = List.of("ON", "QC", "BC");
    when(repository.findDistinctProvinceCodesByCountryCode("CA")).thenReturn(provinces);

    List<String> result = service.getDistinctProvinceCodesByCountryCode("CA");

    assertThat(result).isEqualTo(provinces);
  }
}