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
import org.springframework.test.context.jdbc.Sql;

import web.TestcontainersConfiguration;
import web.user.dto.UserCreateRequest;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = web.WebApplication.class)
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@Sql(scripts = {"classpath:sql/truncate_all.sql", "classpath:sql/test_seed.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserControllerIntegrationTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper objectMapper;

  @MockBean
  FirebaseAuth firebaseAuth;

  @Test
  void create_user_success() throws Exception {
    String token = "dummy-id-token";
    FirebaseToken decoded = Mockito.mock(FirebaseToken.class);
    Mockito.when(decoded.getEmail()).thenReturn("creator@example.com");
    Mockito.when(firebaseAuth.verifyIdToken(eq(token), eq(true))).thenReturn(decoded);

    UserCreateRequest req = new UserCreateRequest(
        "new.user@example.com",
        "1234567890",
        "New User",
        null,
        null,
        null,
        null
    );

    mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", startsWith("/api/v1/users/")))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id", not(emptyString())))
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")))
        .andExpect(jsonPath("$.data.email").value("new.user@example.com"))
        .andExpect(jsonPath("$.data.fullName").value("New User"));
  }

  @Test
  void create_user_duplicate_email_conflict() throws Exception {
    String token = "dummy-id-token";
    FirebaseToken decoded = Mockito.mock(FirebaseToken.class);
    Mockito.when(decoded.getEmail()).thenReturn("creator@example.com");
    Mockito.when(firebaseAuth.verifyIdToken(eq(token), eq(true))).thenReturn(decoded);

    UserCreateRequest req = new UserCreateRequest(
        "dup.user@example.com",
        null,
        "Dup User",
        null,
        null,
        null,
        null
    );

    // First create
    mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id", matchesPattern("^[0-9A-HJKMNP-TV-Z]{26}$")));

    // Second create with same email -> conflict
    mvc.perform(post("/api/v1/users")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"));
  }
}
