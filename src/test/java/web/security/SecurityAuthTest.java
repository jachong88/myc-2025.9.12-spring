package web.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import web.common.dto.ApiResponse;
import web.common.request.RequestIdHolder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = web.WebApplication.class)
@AutoConfigureMockMvc
@Import({SecurityAuthTest.ProtectedController.class, web.TestcontainersConfiguration.class})
@Sql(scripts = {"classpath:sql/truncate_all.sql", "classpath:sql/test_seed.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class SecurityAuthTest {

  @Autowired
  MockMvc mvc;

  @MockBean
  FirebaseAuth firebaseAuth;

  @RestController
  static class ProtectedController {
    @GetMapping("/api/protected-ping")
    public ApiResponse<Map<String, String>> ping() {
      String requestId = RequestIdHolder.getOrCreate();
      return ApiResponse.success(requestId, Map.of("pong", "ok"), null);
    }
  }

  @Test
  void protected_without_token_is_unauthorized() throws Exception {
    mvc.perform(get("/api/protected-ping"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.status").value(401))
      .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
  }

  @Test
  void protected_with_valid_token_is_ok() throws Exception {
    String token = "dummy-id-token";
    FirebaseToken decoded = mock(FirebaseToken.class);
    when(decoded.getEmail()).thenReturn("test@example.com");
    when(firebaseAuth.verifyIdToken(eq(token), eq(true))).thenReturn(decoded);

    mvc.perform(get("/api/protected-ping").header("Authorization", "Bearer " + token))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.pong").value("ok"));
  }
}
