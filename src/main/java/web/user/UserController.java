package web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.common.dto.ApiResponse;
import web.common.request.RequestIdHolder;
import web.user.dto.UserCreateRequest;
import web.user.dto.UserResponse;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
public class UserController {

  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @PostMapping
  @Operation(summary = "Create user")
  public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserCreateRequest req) {
    UserResponse data = service.create(req);
    String rid = RequestIdHolder.getOrCreate();
    return ResponseEntity
        .created(URI.create("/api/v1/users/" + data.id()))
        .body(ApiResponse.success(rid, data, null));
  }
}
