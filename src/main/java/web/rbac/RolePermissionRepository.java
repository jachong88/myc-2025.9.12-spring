package web.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import web.rbac.entity.RolePermissionEntity;

import java.util.Collection;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermissionEntity, String> {
  List<RolePermissionEntity> findByRoleIdIn(Collection<String> roleIds);
}
