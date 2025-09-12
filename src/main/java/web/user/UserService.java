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
import web.user.dto.UserUpdateRequest;
import web.user.dto.UserResponse;
import web.user.entity.UserEntity;

import java.util.Map;
import java.time.Instant;

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

  @Transactional(readOnly = true)
  public Page<web.user.dto.UserListItemResponse> find(
      Integer page, Integer size,
      String countryId, String provinceId,
      String name, String phone, String email,
      String role,
      Boolean deleted
  ) {
    int p = (page == null ? 0 : page);
    int s = (size == null ? 20 : size);
    if (p < 0 || s <= 0 || s > 200) {
      throw new AppException(ErrorCode.ARGUMENT_INVALID, Map.of("page", p, "size", s));
    }

    // Build specification
    org.springframework.data.jpa.domain.Specification<UserEntity> spec = (root, cq, cb) -> {
      java.util.List<jakarta.persistence.criteria.Predicate> preds = new java.util.ArrayList<>();

      // deleted filter
      if (deleted != null) {
        if (deleted) preds.add(cb.isNotNull(root.get("deletedAt")));
        else preds.add(cb.isNull(root.get("deletedAt")));
      } else {
        // default to active only if not specified
        preds.add(cb.isNull(root.get("deletedAt")));
      }

      if (countryId != null && !countryId.isBlank()) {
        preds.add(cb.equal(root.get("countryId"), countryId));
      }
      if (provinceId != null && !provinceId.isBlank()) {
        preds.add(cb.equal(root.get("provinceId"), provinceId));
      }
      if (role != null && !role.isBlank()) {
        // Interpret role as roleId for now
        preds.add(cb.equal(root.get("roleId"), role));
      }
      if (name != null && !name.isBlank()) {
        String like = "%" + name.trim().toLowerCase() + "%";
        preds.add(cb.like(cb.lower(root.get("fullName")), like));
      }
      if (email != null && !email.isBlank()) {
        String like = "%" + email.trim().toLowerCase() + "%";
        preds.add(cb.like(cb.lower(root.get("email")), like));
      }
      if (phone != null && !phone.isBlank()) {
        String like = "%" + phone.trim() + "%";
        preds.add(cb.like(root.get("phone"), like));
      }

      return cb.and(preds.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };

    PageRequest pr = PageRequest.of(p, s, Sort.by("createdAt").descending());
    Page<UserEntity> pageResult = repo.findAll(spec, pr);

    return pageResult.map(u -> new web.user.dto.UserListItemResponse(
        u.getId(),
        u.getFullName(),
        u.getEmail(),
        u.getPhone(),
        u.getRoleId(), // placeholder until roles table provides a name
        u.getCountryId(), // TODO: map to country name if reference table is present
        u.getProvinceId()
    ));
  }

  @Transactional
  public UserResponse update(String id, UserUpdateRequest req) {
    UserEntity e = repo.findByIdAndDeletedAtIsNull(id).orElseThrow(() ->
        new AppException(ErrorCode.RESOURCE_NOT_FOUND, Map.of("id", id)));

    if (req.email() != null) {
      String newEmail = req.email().trim().toLowerCase();
      if (newEmail.isBlank()) {
        throw new AppException(ErrorCode.ARGUMENT_INVALID, Map.of("field", "email", "reason", "blank"));
      }
      String current = e.getEmail() == null ? "" : e.getEmail();
      if (!newEmail.equalsIgnoreCase(current)) {
        if (repo.findByEmailIgnoreCaseAndDeletedAtIsNull(newEmail).isPresent()) {
          throw new AppException(ErrorCode.DUPLICATE_RESOURCE, Map.of("field", "email"));
        }
        e.setEmail(newEmail);
      }
    }

    if (req.phone() != null) {
      String newPhone = req.phone().trim();
      if (newPhone.isBlank()) {
        throw new AppException(ErrorCode.ARGUMENT_INVALID, Map.of("field", "phone", "reason", "blank"));
      }
      String current = e.getPhone() == null ? "" : e.getPhone();
      if (!newPhone.equals(current)) {
        if (repo.findByPhoneAndDeletedAtIsNull(newPhone).isPresent()) {
          throw new AppException(ErrorCode.DUPLICATE_RESOURCE, Map.of("field", "phone"));
        }
        e.setPhone(newPhone);
      }
    }

    if (req.fullName() != null) {
      String name = req.fullName().trim();
      if (name.isBlank()) {
        throw new AppException(ErrorCode.ARGUMENT_INVALID, Map.of("field", "fullName", "reason", "blank"));
      }
      e.setFullName(name);
    }

    if (req.isActive() != null) {
      e.setIsActive(req.isActive());
    }
    if (req.countryId() != null) e.setCountryId(req.countryId());
    if (req.provinceId() != null) e.setProvinceId(req.provinceId());
    if (req.roleId() != null) e.setRoleId(req.roleId());

    try {
      UserEntity saved = repo.save(e);
      return toResponse(saved);
    } catch (DataIntegrityViolationException ex) {
      throw new AppException(ErrorCode.CONFLICT, Map.of("reason", "constraint_violation"));
    }
  }

  @Transactional
  public void softDelete(String id) {
    UserEntity e = repo.findByIdAndDeletedAtIsNull(id).orElseThrow(() ->
        new AppException(ErrorCode.RESOURCE_NOT_FOUND, Map.of("id", id)));
    e.setDeletedAt(Instant.now());
    repo.save(e);
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
