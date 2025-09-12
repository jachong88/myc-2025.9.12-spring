package web.me;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import web.common.dto.ApiResponse;
import web.common.request.RequestIdHolder;
import web.rbac.AuthorizationService;
import web.common.security.CurrentUser;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

  private final AuthorizationService authz;

  public MeController(AuthorizationService authz) {
    this.authz = authz;
  }

  @GetMapping("/permissions")
  public ResponseEntity<ApiResponse<web.rbac.AuthorizationService.EffectivePermissions>> permissions() {
    String requestId = RequestIdHolder.getOrCreate();
    String email = CurrentUser.email().orElse(null);
    String userId = null;
    if (email != null) {
      userId = authz.resolveUserIdByEmail(email).orElse(null);
    }
    var effective = authz.computeForUserId(userId);
    return ResponseEntity.ok(ApiResponse.success(requestId, effective, null));
  }
}
