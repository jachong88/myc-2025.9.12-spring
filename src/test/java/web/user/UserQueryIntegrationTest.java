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
import web.user.dto.UserCreateRequest;
import com.web.TestcontainersConfiguration;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.web.WebApplication.class)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql(scripts = {"classpath:sql/truncate_all.sql", "classpath:sql/test_seed.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserQueryIntegrationTest {

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
  void get_by_id_ok_and_not_found() throws Exception {
    String token = "dummy-id-token";
    mockAuth(token, "creator@example.com");

    // Create one
    UserCreateRequest req = new UserCreateRequest(
        "lookup.user@example.com", null, "Lookup User", null, null, null, null
    );

    String content = mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")))
        .andReturn().getResponse().getContentAsString();

    JsonNode root = objectMapper.readTree(content);
    String id = root.at("/data/id").asText();

    // GET by id -> 200
    mvc.perform(get("/api/v1/users/{id}", id)
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(id))
        .andExpect(jsonPath("$.data.email").value("lookup.user@example.com"));

    // GET invalid id -> 404
    mvc.perform(get("/api/v1/users/{id}", "01H9999999999999999999999")
            .header("Authorization", "Bearer " + token))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"));
  }

  @Test
  void list_users_pagination() throws Exception {
    String token = "dummy-id-token";
    mockAuth(token, "creator@example.com");

    // Seed 3 users
    for (int i = 0; i < 3; i++) {
      UserCreateRequest req = new UserCreateRequest(
          "list.user." + i + "@example.com",
          null,
          "List User " + i,
          null, null, null, null
      );
      mvc.perform(post("/api/v1/users")
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(req)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")));
    }

    // Page 0 size 2
    mvc.perform(get("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .param("page", "0")
            .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.meta.page").value(0))
        .andExpect(jsonPath("$.meta.size").value(2))
        .andExpect(jsonPath("$.meta.totalItems", greaterThanOrEqualTo(3)))
        .andExpect(jsonPath("$.meta.totalPages", greaterThanOrEqualTo(2)))
        .andExpect(jsonPath("$.meta.hasNext").value(true));

    // Page 1 size 2 (should have at least 1 item)
    mvc.perform(get("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .param("page", "1")
            .param("size", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$.meta.page").value(1))
        .andExpect(jsonPath("$.meta.size").value(2));
  }
}
