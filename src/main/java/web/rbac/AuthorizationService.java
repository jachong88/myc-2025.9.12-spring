package web.rbac;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import web.user.entity.UserEntity;
import web.rbac.entity.UserRoleEntity;
import web.rbac.entity.RolePermissionEntity;
import web.rbac.entity.PermissionEntity;
import web.rbac.entity.UserScopeEntity;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthorizationService {

  @Value("${app.rbac.enforce:false}")
  private boolean enforce;

  private final web.user.UserRepository userRepo;
  private final UserRoleRepository userRoleRepo;
  private final RolePermissionRepository rolePermRepo;
  private final PermissionRepository permRepo;
  private final UserScopeRepository userScopeRepo;

  public AuthorizationService(web.user.UserRepository userRepo,
                              UserRoleRepository userRoleRepo,
                              RolePermissionRepository rolePermRepo,
                              PermissionRepository permRepo,
                              UserScopeRepository userScopeRepo) {
    this.userRepo = userRepo;
    this.userRoleRepo = userRoleRepo;
    this.rolePermRepo = rolePermRepo;
    this.permRepo = permRepo;
    this.userScopeRepo = userScopeRepo;
  }

  public record EffectivePermission(String resource, String action, String scope, String effect, List<String> scopeIds) {}
  public record EffectivePermissions(String userId, List<String> roles, List<EffectivePermission> permissions) {}

  public Optional<String> resolveUserIdByEmail(String email) {
    return userRepo.findByEmailIgnoreCaseAndDeletedAtIsNull(email).map(UserEntity::getId);
  }

  public EffectivePermissions computeForUserId(String userId) {
    if (userId == null) {
      return new EffectivePermissions(null, List.of(), List.of());
    }

    List<UserRoleEntity> userRoles = userRoleRepo.findByUserId(userId);
    List<String> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).toList();

    // For brevity, we don't load role names here (not strictly needed for this slice)
    List<String> roleNames = List.of();

    List<RolePermissionEntity> rps = rolePermRepo.findByRoleIdIn(roleIds);
    List<String> permIds = rps.stream().map(RolePermissionEntity::getPermissionId).toList();
    List<PermissionEntity> perms = permRepo.findByIdIn(permIds);

    // Load user scopes
    List<UserScopeEntity> scopes = userScopeRepo.findByUserId(userId);
    Map<String, List<String>> scopeIdsByType = scopes.stream()
        .collect(Collectors.groupingBy(UserScopeEntity::getScopeType,
            Collectors.mapping(UserScopeEntity::getScopeId, Collectors.toList())));

    // Build effective entries; include scopeIds that match the permission scope
    List<EffectivePermission> eff = new ArrayList<>();
    for (PermissionEntity p : perms) {
      List<String> scopeIds = null;
      String s = p.getScope();
      if (!"GLOBAL".equalsIgnoreCase(s)) {
        scopeIds = scopeIdsByType.getOrDefault(s.toUpperCase(), List.of());
      }
      eff.add(new EffectivePermission(p.getResource(), p.getAction(), p.getScope(), p.getEffect(), scopeIds));
    }

    return new EffectivePermissions(userId, roleNames, eff);
  }

  // Build additional filter for viewing Users based on scopes.
  public Specification<UserEntity> userViewSpecForUserId(String userId) {
    if (!enforce) {
      return (root, cq, cb) -> cb.conjunction();
    }
    if (userId == null) {
      return (root, cq, cb) -> cb.disjunction(); // deny
    }

    // Gather permissions for USER:VIEW
    List<UserRoleEntity> userRoles = userRoleRepo.findByUserId(userId);
    List<String> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).toList();
    List<RolePermissionEntity> rps = rolePermRepo.findByRoleIdIn(roleIds);
    List<String> permIds = rps.stream().map(RolePermissionEntity::getPermissionId).toList();
    List<PermissionEntity> perms = permRepo.findByIdIn(permIds).stream()
        .filter(p -> "USER".equalsIgnoreCase(p.getResource()) && "VIEW".equalsIgnoreCase(p.getAction()))
        .toList();

    // Deny precedence
    boolean anyDeny = perms.stream().anyMatch(p -> "DENY".equalsIgnoreCase(p.getEffect()));
    if (anyDeny) {
      return (root, cq, cb) -> cb.disjunction();
    }

    boolean hasGlobal = perms.stream().anyMatch(p -> "GLOBAL".equalsIgnoreCase(p.getScope()));
    if (hasGlobal) {
      return (root, cq, cb) -> cb.conjunction();
    }

    // Country/province scopes
    List<UserScopeEntity> scopes = userScopeRepo.findByUserId(userId);
    List<String> countryIds = scopes.stream()
        .filter(s -> "COUNTRY".equalsIgnoreCase(s.getScopeType()))
        .map(UserScopeEntity::getScopeId).toList();
    List<String> provinceIds = scopes.stream()
        .filter(s -> "PROVINCE".equalsIgnoreCase(s.getScopeType()))
        .map(UserScopeEntity::getScopeId).toList();

    return (root, cq, cb) -> {
      List<jakarta.persistence.criteria.Predicate> ors = new ArrayList<>();
      if (!countryIds.isEmpty()) {
        ors.add(root.get("countryId").in(countryIds));
      }
      if (!provinceIds.isEmpty()) {
        ors.add(root.get("provinceId").in(provinceIds));
      }
      if (ors.isEmpty()) {
        return cb.disjunction(); // deny (no scopes)
      }
      return cb.or(ors.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
  }
}
