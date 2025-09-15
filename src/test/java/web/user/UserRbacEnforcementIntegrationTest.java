package web.user;

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
class UserRbacEnforcementIntegrationTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean FirebaseAuth firebaseAuth;

  private void mockAuth(String token, String email) throws Exception {
    FirebaseToken decoded = Mockito.mock(FirebaseToken.class);
    Mockito.when(decoded.getEmail()).thenReturn(email);
    Mockito.when(firebaseAuth.verifyIdToken(eq(token), eq(true))).thenReturn(decoded);
  }

  @Test
  void user_create_update_delete_allowed_for_HQ() throws Exception {
    String token = "dummy-id-token";
    // Seeded test migration assigns HQ role to creator@example.com with GLOBAL USER perms
    mockAuth(token, "creator@example.com");

    // Create
    UserCreateRequest req = new UserCreateRequest("rbac.user@example.com", null, "Rbac User", null, null, null, null);
    String created = mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")))
        .andReturn().getResponse().getContentAsString();
    String id = objectMapper.readTree(created).at("/data/id").asText();

    // Update
    mvc.perform(patch("/api/v1/users/{id}", id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"Rbac User Updated\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.fullName").value("Rbac User Updated"));

    // Delete
    mvc.perform(delete("/api/v1/users/{id}", id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void user_create_denied_without_permissions() throws Exception {
    String token = "noperms-token";
    mockAuth(token, "no.perms@example.com");

    UserCreateRequest req = new UserCreateRequest("noauth.create@example.com", null, "No Auth", null, null, null, null);
    mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
  }

  @Test
  void user_update_delete_denied_without_permissions() throws Exception {
    // First create a target user using HQ account
    String adminToken = "admin-token";
    mockAuth(adminToken, "creator@example.com");
    UserCreateRequest req = new UserCreateRequest("target.update@example.com", null, "Target Update", null, null, null, null);
    String created = mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")))
        .andReturn().getResponse().getContentAsString();
    String id = objectMapper.readTree(created).at("/data/id").asText();

    // Now attempt update/delete as a user with no permissions
    String token = "noperms-token2";
    mockAuth(token, "another.no.perms@example.com");

    mvc.perform(patch("/api/v1/users/{id}", id)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"fullName\":\"Should Not Update\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));

    mvc.perform(delete("/api/v1/users/{id}", id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
  }
}
