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
class UserFindIntegrationTest {

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
  void find_users_filters_and_pagination() throws Exception {
    String token = "dummy-id-token";
    mockAuth(token, "creator@example.com");

    // Seed users
    mvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new UserCreateRequest("alice@example.com", "111", "Alice Tan", "SG", "SG-01", "Teacher", null))))
      .andExpect(status().isCreated());

    mvc.perform(post("/api/v1/users").header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new UserCreateRequest("bob@example.com", "222", "Bob Lee", "SG", "SG-02", "Owner", null))))
      .andExpect(status().isCreated());

    // Filter by name contains 'Alice'
    mvc.perform(get("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .param("name", "Alice"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$.data[0].name", containsStringIgnoringCase("alice")));

    // Filter by email contains 'bob'
    mvc.perform(get("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .param("email", "bob"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].email", containsStringIgnoringCase("bob")));

    // Pagination
    mvc.perform(get("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .param("page", "0").param("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.page").value(0))
        .andExpect(jsonPath("$.meta.size").value(1));
  }
}
