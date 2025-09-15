package web.studio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import web.common.util.Ulids;
import web.location.AddressRepository;
import web.location.PostalCodeReferenceRepository;
import web.location.entity.AddressEntity;
import web.location.entity.PostalCodeReferenceEntity;
import web.studio.dto.StudioCreateRequest;
import web.studio.entity.StudioEntity;
import web.user.UserRepository;
import web.user.entity.UserEntity;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class StudioControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudioRepository studioRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PostalCodeReferenceRepository postalCodeRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testOwner;
    private PostalCodeReferenceEntity testPostalCode;
    private String currentUserId;

    @BeforeEach
    void setUp() {
        // Create test user as owner
        testOwner = new UserEntity();
testOwner.setId(Ulids.newUlid());
        testOwner.setEmail("studio.owner@example.com");
        testOwner.setFullName("Studio Owner");
        testOwner.setPhone("+65-1234-5678");
        testOwner.setIsActive(true);
testOwner.setCreatedAt(java.time.Instant.now());
        testOwner.setCreatedBy("test-system");
        testOwner = userRepository.save(testOwner);

        // Create test postal code reference
        testPostalCode = new PostalCodeReferenceEntity();
testPostalCode.setId(Ulids.newUlid());
        testPostalCode.setPostalCode("018915");
        testPostalCode.setCity("Central Boulevard");
        testPostalCode.setProvinceCode("SG");
        testPostalCode.setCountryCode("SG");
        testPostalCode.setStatus("active");
        testPostalCode.setCreatedAt(java.time.Instant.now());
        testPostalCode.setCreatedBy("test-system");
        testPostalCode = postalCodeRepository.save(testPostalCode);

        currentUserId = testOwner.getId();
    }

    @Test
    void createStudio_Success() throws Exception {
        // Given
        StudioCreateRequest request = createValidStudioRequest();

        // When & Then
        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.name", is("ABC Dance Studio")))
                .andExpect(jsonPath("$.data.code", is("ABC001")))
                .andExpect(jsonPath("$.data.companyName", is("ABC Dance Pte Ltd")))
                .andExpect(jsonPath("$.data.email", is("contact@abcdance.com")))
                .andExpect(jsonPath("$.data.phone", is("+65-1234-5678")))
                .andExpect(jsonPath("$.data.status", is("active")))
                .andExpect(jsonPath("$.data.owner.id", is(testOwner.getId())))
                .andExpect(jsonPath("$.data.owner.fullName", is("Studio Owner")))
                .andExpect(jsonPath("$.data.address.streetLine1", is("123 Marina Bay Street")))
                .andExpect(jsonPath("$.data.address.streetLine2", is("Unit 45-67")))
                .andExpect(jsonPath("$.data.address.city", is("Central Boulevard")))
                .andExpect(jsonPath("$.data.address.postalCode.postalCode", is("018915")))
                .andExpect(jsonPath("$.data.address.postalCode.countryCode", is("SG")))
                .andExpect(jsonPath("$.data.createdAt", notNullValue()));

        // Verify database state
        Optional<StudioEntity> savedStudio = studioRepository.findByCodeIgnoreCaseAndStatusAndDeletedAtIsNull(
                "ABC001", StudioEntity.StudioStatus.ACTIVE);
        assertThat(savedStudio).isPresent();
        assertThat(savedStudio.get().getName()).isEqualTo("ABC Dance Studio");
        assertThat(savedStudio.get().getOwner().getId()).isEqualTo(testOwner.getId());

        // Verify address was created
        Optional<AddressEntity> savedAddress = addressRepository.findActiveById(savedStudio.get().getAddress().getId());
        assertThat(savedAddress).isPresent();
        assertThat(savedAddress.get().getStreetLine1()).isEqualTo("123 Marina Bay Street");
        assertThat(savedAddress.get().getPostalCodeReference().getId()).isEqualTo(testPostalCode.getId());
    }

    @Test
    void createStudio_ValidationFailure_MissingRequiredFields() throws Exception {
        // Given - request with missing required fields
        StudioCreateRequest request = new StudioCreateRequest();
        request.setName(""); // Invalid empty name
        // Missing code, companyName, ownerId, address

        // When & Then
        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))));
    }

    @Test
    void createStudio_DuplicateEmail_Conflict() throws Exception {
        // Given - create first studio
        StudioCreateRequest firstRequest = createValidStudioRequest();
        firstRequest.setCode("FIRST001");
        firstRequest.setEmail("duplicate@example.com");

        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - create second studio with same email
        StudioCreateRequest secondRequest = createValidStudioRequest();
        secondRequest.setCode("SECOND001");
        secondRequest.setEmail("duplicate@example.com"); // Duplicate email

        // Then
        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Email already exists")));
    }

    @Test
    void createStudio_DuplicateCode_Conflict() throws Exception {
        // Given - create first studio
        StudioCreateRequest firstRequest = createValidStudioRequest();
        firstRequest.setCode("DUPLICATE001");

        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // When - create second studio with same code
        StudioCreateRequest secondRequest = createValidStudioRequest();
        secondRequest.setCode("DUPLICATE001"); // Duplicate code
        secondRequest.setEmail("different@example.com");

        // Then
        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Studio code already exists")));
    }

    @Test
    void createStudio_InvalidPostalCode_NotFound() throws Exception {
        // Given - request with non-existent postal code
        StudioCreateRequest request = createValidStudioRequest();
request.getAddress().setPostalCodeId(Ulids.newUlid());

        // When & Then
        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Postal code not found")));
    }

    @Test
    void createStudio_InvalidOwner_NotFound() throws Exception {
        // Given - request with non-existent owner
        StudioCreateRequest request = createValidStudioRequest();
request.setOwnerId(Ulids.newUlid());

        // When & Then
        mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Owner not found")));
    }

    @Test
    void getStudioById_Success() throws Exception {
        // Given - create a studio first
        StudioCreateRequest createRequest = createValidStudioRequest();
        
        String response = mockMvc.perform(post("/api/studios")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Current-User-Id", currentUserId)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String studioId = objectMapper.readTree(response).get("data").get("id").asText();

        // When & Then - get studio by ID
        mockMvc.perform(get("/api/studios/{id}", studioId)
                .header("X-Current-User-Id", currentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(studioId)))
                .andExpect(jsonPath("$.data.name", is("ABC Dance Studio")));
    }

    @Test
    void getStudioById_NotFound() throws Exception {
        // Given - non-existent studio ID
String nonExistentId = Ulids.newUlid();

        // When & Then
        mockMvc.perform(get("/api/studios/{id}", nonExistentId)
                .header("X-Current-User-Id", currentUserId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    void getStudiosByOwner_Success() throws Exception {
        // Given - create multiple studios for the owner
        for (int i = 1; i <= 3; i++) {
            StudioCreateRequest request = createValidStudioRequest();
            request.setName("Studio " + i);
            request.setCode("STUDIO" + String.format("%03d", i));
            request.setEmail("studio" + i + "@example.com");

            mockMvc.perform(post("/api/studios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Current-User-Id", currentUserId)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // When & Then - get studios by owner
        mockMvc.perform(get("/api/studios/owner/{ownerId}", testOwner.getId())
                .header("X-Current-User-Id", currentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[*].owner.id", everyItem(is(testOwner.getId()))));
    }

    @Test
    void searchPostalCodes_Autocomplete_Success() throws Exception {
        // Given - additional postal codes for testing
        createTestPostalCode("018916", "Marina Gardens Drive", "SG", "SG");
        createTestPostalCode("018920", "Central Boulevard", "SG", "SG");

        // When & Then - search postal codes
        mockMvc.perform(get("/api/postal-codes/search")
                .param("q", "0189")
                .param("country", "SG")
                .param("limit", "10")
                .header("X-Current-User-Id", currentUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].postalCode", startsWith("0189")))
                .andExpect(jsonPath("$.data[0].countryCode", is("SG")))
                .andExpect(jsonPath("$.data[0].displayText", notNullValue()));
    }

    // Helper methods

    private StudioCreateRequest createValidStudioRequest() {
        StudioCreateRequest.AddressCreateRequest address = 
            new StudioCreateRequest.AddressCreateRequest();
        address.setPostalCodeId(testPostalCode.getId());
        address.setStreetLine1("123 Marina Bay Street");
        address.setStreetLine2("Unit 45-67");
        address.setCity("Central Boulevard");
        address.setAttention("Reception");

        StudioCreateRequest request = new StudioCreateRequest();
        request.setName("ABC Dance Studio");
        request.setCode("ABC001");
        request.setPhone("+65-1234-5678");
        request.setEmail("contact@abcdance.com");
        request.setCompanyName("ABC Dance Pte Ltd");
        request.setCompanyRegistrationNo("202012345A");
        request.setOwnerId(testOwner.getId());
        request.setNote("Main studio location");
        request.setAddress(address);

        return request;
    }

    private PostalCodeReferenceEntity createTestPostalCode(String postalCode, String city, 
                                                          String provinceCode, String countryCode) {
        PostalCodeReferenceEntity entity = new PostalCodeReferenceEntity();
entity.setId(Ulids.newUlid());
        entity.setPostalCode(postalCode);
        entity.setCity(city);
        entity.setProvinceCode(provinceCode);
        entity.setCountryCode(countryCode);
        entity.setStatus("active");
        entity.setCreatedAt(java.time.Instant.now());
        entity.setCreatedBy("test-system");
        return postalCodeRepository.save(entity);
    }
}