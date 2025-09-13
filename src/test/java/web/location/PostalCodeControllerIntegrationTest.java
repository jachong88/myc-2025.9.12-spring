package web.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import web.location.dto.PostalCodeCreateRequest;
import web.location.entity.PostalCodeReferenceEntity;
import com.github.f4b6a3.ulid.UlidCreator;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@Transactional
class PostalCodeControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostalCodeReferenceRepository repository;

  @Autowired
  private ObjectMapper objectMapper;

  private PostalCodeReferenceEntity testEntity1;
  private PostalCodeReferenceEntity testEntity2;

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
    
    repository.saveAll(List.of(testEntity1, testEntity2));
  }

  @Test
  void testSearchPostalCodes_Success() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/search")
            .param("countryCode", "CA")
            .param("query", "M4B")
            .param("page", "0")
            .param("size", "20")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.data[0].postalCode").value(startsWith("M4B")))
        .andExpect(jsonPath("$.data[0].countryCode").value("CA"))
        .andExpect(jsonPath("$.data[0].city").value("Toronto"))
        .andExpect(jsonPath("$.meta.page").value(0))
        .andExpect(jsonPath("$.meta.size").value(20))
        .andExpect(jsonPath("$.meta.totalItems").value(2));
  }

  @Test
  void testSearchPostalCodes_EmptyResult() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/search")
            .param("countryCode", "CA")
            .param("query", "XXX")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(0)))
        .andExpect(jsonPath("$.meta.totalItems").value(0));
  }

  @Test
  void testValidatePostalCode_Valid() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/validate")
            .param("countryCode", "CA")
            .param("postalCode", "M4B 1A1")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.valid").value(true))
        .andExpect(jsonPath("$.data.postalCode.postalCode").value("M4B 1A1"))
        .andExpect(jsonPath("$.data.postalCode.city").value("Toronto"))
        .andExpect(jsonPath("$.data.postalCode.countryCode").value("CA"));
  }

  @Test
  void testValidatePostalCode_NotFound() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/validate")
            .param("countryCode", "CA")
            .param("postalCode", "M9Z 9Z9")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.valid").value(false))
        .andExpect(jsonPath("$.data.postalCode").isEmpty())
        .andExpect(jsonPath("$.data.message").value(containsString("Valid format but not found")));
  }

  @Test
  void testValidatePostalCode_InvalidFormat() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/validate")
            .param("countryCode", "CA")
            .param("postalCode", "INVALID")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.valid").value(false))
        .andExpect(jsonPath("$.data.message").value(containsString("Invalid postal code format")));
  }

  @Test
  void testGetCitiesByCountry() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/cities")
            .param("countryCode", "CA")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0]").value("Toronto"))
        .andExpect(jsonPath("$.meta.count").value(1));
  }

  @Test
  void testGetProvincesByCountry() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/provinces")
            .param("countryCode", "CA")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0]").value("ON"))
        .andExpect(jsonPath("$.meta.count").value(1));
  }

  @Test
  void testSearchByCity() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/search/city")
            .param("countryCode", "CA")
            .param("city", "Toronto")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.data[0].city").value("Toronto"))
        .andExpect(jsonPath("$.meta.totalItems").value(2));
  }

  @Test
  void testSearchByProvince() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/search/province")
            .param("countryCode", "CA")
            .param("provinceCode", "ON")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.data[0].provinceCode").value("ON"))
        .andExpect(jsonPath("$.meta.totalItems").value(2));
  }

  @Test
  void testGetStatistics() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/stats/CA")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.countryCode").value("CA"))
        .andExpect(jsonPath("$.data.totalPostalCodes").value(2))
        .andExpect(jsonPath("$.data.provinceCount").value(1))
        .andExpect(jsonPath("$.data.cityCount").value(1));
  }

  @Test
  void testCreatePostalCode_Success() throws Exception {
    PostalCodeCreateRequest request = new PostalCodeCreateRequest(
        "M5V 1A1", "Toronto", "ON", "CA");
    
    mockMvc.perform(post("/api/v1/postal-codes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.postalCode").value("M5V 1A1"))
        .andExpect(jsonPath("$.data.city").value("Toronto"))
        .andExpect(jsonPath("$.data.provinceCode").value("ON"))
        .andExpect(jsonPath("$.data.countryCode").value("CA"))
        .andExpect(jsonPath("$.data.status").value("active"));
  }

  @Test
  void testCreatePostalCode_ValidationError() throws Exception {
    PostalCodeCreateRequest request = new PostalCodeCreateRequest(
        "", "Toronto", "ON", "CA"); // Empty postal code
    
    mockMvc.perform(post("/api/v1/postal-codes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreatePostalCode_Duplicate() throws Exception {
    PostalCodeCreateRequest request = new PostalCodeCreateRequest(
        "M4B 1A1", "Toronto", "ON", "CA"); // Already exists
    
    mockMvc.perform(post("/api/v1/postal-codes")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testGetById_Found() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/" + testEntity1.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(testEntity1.getId()))
        .andExpect(jsonPath("$.data.postalCode").value("M4B 1A1"));
  }

  @Test
  void testGetById_NotFound() throws Exception {
    mockMvc.perform(get("/api/v1/postal-codes/nonexistent-id")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  void testSearchPostalCodes_InvalidParameters() throws Exception {
    // Missing country code
    mockMvc.perform(get("/api/v1/postal-codes/search")
            .param("query", "M4B")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    
    // Invalid country code length
    mockMvc.perform(get("/api/v1/postal-codes/search")
            .param("countryCode", "INVALID")
            .param("query", "M4B")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    
    // Missing query
    mockMvc.perform(get("/api/v1/postal-codes/search")
            .param("countryCode", "CA")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}