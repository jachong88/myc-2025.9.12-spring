package web.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import web.rbac.entity.PermissionEntity;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntity, String> {
  List<PermissionEntity> findByIdIn(Collection<String> ids);
  List<PermissionEntity> findByResourceAndAction(String resource, String action);
}
