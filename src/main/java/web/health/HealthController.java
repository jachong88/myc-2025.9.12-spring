package web.health;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.dto.ApiResponse;
import web.common.request.RequestIdHolder;

@RestController
public class HealthController {

  @GetMapping(path = "/api/v1/health", produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<Map<String, String>> health() {
    String requestId = RequestIdHolder.getOrCreate();
    return ApiResponse.success(requestId, Map.of("status", "ok"), null);
  }
}
