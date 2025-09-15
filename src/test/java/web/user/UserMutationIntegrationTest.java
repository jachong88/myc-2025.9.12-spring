package web.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import web.TestcontainersConfiguration;
import web.user.dto.UserCreateRequest;

import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = web.WebApplication.class)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql(scripts = {"classpath:sql/truncate_all.sql", "classpath:sql/test_seed.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserMutationIntegrationTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  FirebaseAuth firebaseAuth;

  private void mockAuth(String token, String email) throws Exception {
    FirebaseToken decoded = Mockito.mock(FirebaseToken.class);
    Mockito.when(decoded.getEmail()).thenReturn(email);
    Mockito.when(firebaseAuth.verifyIdToken(eq(token), eq(true))).thenReturn(decoded);
  }

  @Test
  void update_user_success_and_conflict() throws Exception {
    String token = "dummy-id-token";
    mockAuth(token, "creator@example.com");

    // Create U1
    UserCreateRequest u1 = new UserCreateRequest("u1@example.com", null, "User One", null, null, null, null);
    String u1Resp = mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(u1)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")))
        .andReturn().getResponse().getContentAsString();
    String u1Id = objectMapper.readTree(u1Resp).at("/data/id").asText();

    // Create U2
    UserCreateRequest u2 = new UserCreateRequest("u2@example.com", null, "User Two", null, null, null, null);
    String u2Resp = mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(u2)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")))
        .andReturn().getResponse().getContentAsString();
    String u2Id = objectMapper.readTree(u2Resp).at("/data/id").asText();

    // Update U1 fullName and phone
    String patchBody = """
      {"phone":"999","fullName":"User One Updated"}
    """;
    mvc.perform(patch("/api/v1/users/{id}", u1Id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(patchBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.fullName").value("User One Updated"))
        .andExpect(jsonPath("$.data.phone").value("999"));

    // Update U2 email to U1 email -> conflict
    String conflictBody = """
      {"email":"u1@example.com"}
    """;
    mvc.perform(patch("/api/v1/users/{id}", u2Id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(conflictBody))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
  }

  @Test
  void soft_delete_then_recreate_with_same_email() throws Exception {
    String token = "dummy-id-token";
    mockAuth(token, "creator@example.com");

    // Create
    UserCreateRequest req = new UserCreateRequest("to.delete@example.com", null, "To Delete", null, null, null, null);
    String resp = mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")))
        .andReturn().getResponse().getContentAsString();
    String id = objectMapper.readTree(resp).at("/data/id").asText();

    // Delete
    mvc.perform(delete("/api/v1/users/{id}", id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // GET after delete -> 404
    mvc.perform(get("/api/v1/users/{id}", id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound());

    // Re-create with same email -> should succeed
    mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")));
  }
}
