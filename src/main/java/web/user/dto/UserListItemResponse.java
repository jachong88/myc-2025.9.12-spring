package web.user.dto;

public record UserListItemResponse(
    String id,
    String name,
    String email,
    String phone,
    String role,
    String country,
    String province
) {}