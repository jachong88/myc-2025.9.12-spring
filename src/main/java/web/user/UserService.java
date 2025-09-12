package web.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.common.exception.AppException;
import web.common.exception.ErrorCode;
import web.common.util.Ulids;
import web.user.dto.UserCreateRequest;
import web.user.dto.UserResponse;
import web.user.entity.UserEntity;

import java.util.Map;

@Service
public class UserService {

  private final UserRepository repo;

  public UserService(UserRepository repo) {
    this.repo = repo;
  }

  @Transactional
  public UserResponse create(UserCreateRequest req) {
    // Validate at-least-one contact field
    boolean hasEmail = req.email() != null && !req.email().isBlank();
    boolean hasPhone = req.phone() != null && !req.phone().isBlank();
    if (!hasEmail && !hasPhone) {
      throw new AppException(
          ErrorCode.ARGUMENT_INVALID,
          Map.of("reason", "email_or_phone_required"));
    }

    String email = hasEmail ? req.email().trim().toLowerCase() : null;
    String phone = hasPhone ? req.phone().trim() : null;

    // Pre-check duplicates for clearer errors (active rows only)
    if (email != null && repo.findByEmailIgnoreCaseAndDeletedAtIsNull(email).isPresent()) {
      throw new AppException(
          ErrorCode.DUPLICATE_RESOURCE,
          Map.of("field", "email"));
    }
    if (phone != null && repo.findByPhoneAndDeletedAtIsNull(phone).isPresent()) {
      throw new AppException(
          ErrorCode.DUPLICATE_RESOURCE,
          Map.of("field", "phone"));
    }

    UserEntity e = new UserEntity();
    e.setId(Ulids.newUlid());
    e.setEmail(email);
    e.setPhone(phone);
    e.setFullName(req.fullName().trim());
    e.setIsActive(req.isActive() == null ? true : req.isActive());
    e.setCountryId(req.countryId());
    e.setProvinceId(req.provinceId());
    e.setRoleId(req.roleId());

    try {
      UserEntity saved = repo.save(e);
      return toResponse(saved);
    } catch (DataIntegrityViolationException ex) {
      // Fallback in case of race condition with unique indexes
      throw new AppException(
          ErrorCode.DUPLICATE_RESOURCE,
          Map.of("field", "email_or_phone"));
    }
  }

  @Transactional(readOnly = true)
  public UserResponse getById(String id) {
    return repo.findByIdAndDeletedAtIsNull(id)
        .map(UserService::toResponse)
        .orElseThrow(() -> new AppException(
            ErrorCode.RESOURCE_NOT_FOUND,
            Map.of("id", id)));
  }

  @Transactional(readOnly = true)
  public Page<UserResponse> list(int page, int size) {
    if (page < 0 || size <= 0 || size > 200) {
      throw new AppException(
          ErrorCode.ARGUMENT_INVALID,
          Map.of("page", page, "size", size));
    }
    PageRequest pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
    return repo.findByDeletedAtIsNull(pr).map(UserService::toResponse);
  }

  private static UserResponse toResponse(UserEntity e) {
    return new UserResponse(
        e.getId(),
        e.getEmail(),
        e.getPhone(),
        e.getFullName(),
        e.isActive(),
        e.getCountryId(),
        e.getProvinceId(),
        e.getRoleId(),
        e.getCreatedAt(),
        e.getUpdatedAt()
    );
  }
}
