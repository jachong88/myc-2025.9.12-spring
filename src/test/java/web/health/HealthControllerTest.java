package web.health;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = com.web.WebApplication.class)
@AutoConfigureMockMvc
class HealthControllerTest {

  @Autowired
  MockMvc mvc;

  @Test
  void health_returns_ok_envelope() throws Exception {
    mvc.perform(get("/api/v1/health"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.success").value(true))
      .andExpect(jsonPath("$.data.status").value("ok"))
      .andExpect(jsonPath("$.requestId").isString());
  }
}
