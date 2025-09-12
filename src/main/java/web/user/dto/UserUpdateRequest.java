package web.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Email(message = "Invalid email")
    @Size(max = 320)
    String email,

    @Size(max = 20)
    String phone,

    @Size(max = 255)
    String fullName,

    @Size(max = 26)
    String countryId,

    @Size(max = 26)
    String provinceId,

    @Size(max = 26)
    String roleId,

    Boolean isActive
) {}
