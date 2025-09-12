package web.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import web.common.dto.ApiResponse;
import web.common.request.RequestIdHolder;
import web.user.dto.UserCreateRequest;
import web.user.dto.UserUpdateRequest;
import web.user.dto.UserResponse;
import web.user.dto.UserListItemResponse;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

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

  @GetMapping("/{id}")
  @Operation(summary = "Get user by id")
  public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable String id) {
    UserResponse data = service.getById(id);
    String rid = RequestIdHolder.getOrCreate();
    return ResponseEntity.ok(ApiResponse.success(rid, data, null));
  }

  @GetMapping
  @Operation(summary = "List users (paginated)")
  public ResponseEntity<ApiResponse<List<UserListItemResponse>>> list(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size,
      @RequestParam(required = false) String countryId,
      @RequestParam(required = false) String provinceId,
      @RequestParam(required = false, name = "name") String name,
      @RequestParam(required = false) String phone,
      @RequestParam(required = false) String email,
      @RequestParam(required = false, name = "role") String role,
      @RequestParam(required = false) Boolean deleted
  ) {
    Page<UserListItemResponse> result = service.find(page, size, countryId, provinceId, name, phone, email, role, deleted);
    String rid = RequestIdHolder.getOrCreate();
    Map<String, Object> meta = Map.of(
        "page", (page == null ? 0 : page),
        "size", (size == null ? 20 : size),
        "totalItems", result.getTotalElements(),
        "totalPages", result.getTotalPages(),
        "hasNext", result.hasNext()
    );
    return ResponseEntity.ok(ApiResponse.success(rid, result.getContent(), meta));
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update user (partial)")
  public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable String id,
                                                          @Valid @RequestBody UserUpdateRequest req) {
    UserResponse data = service.update(id, req);
    String rid = RequestIdHolder.getOrCreate();
    return ResponseEntity.ok(ApiResponse.success(rid, data, null));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Soft delete user")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
    service.softDelete(id);
    String rid = RequestIdHolder.getOrCreate();
    return ResponseEntity.ok(ApiResponse.success(rid, null, null));
  }
}
