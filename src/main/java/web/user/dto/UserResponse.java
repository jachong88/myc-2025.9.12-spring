package web.user.dto;

import java.time.Instant;

public record UserResponse(
    String id,
    String email,
    String phone,
    String fullName,
    boolean isActive,
    String countryId,
    String provinceId,
    String roleId,
    Instant createdAt,
    Instant updatedAt
) {}
