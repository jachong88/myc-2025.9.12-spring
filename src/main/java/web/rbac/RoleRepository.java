package web.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import web.rbac.entity.RoleEntity;

import java.util.Collection;
import java.util.List;

public interface RoleRepository extends JpaRepository<RoleEntity, String> {
  List<RoleEntity> findByIdIn(Collection<String> ids);
  List<RoleEntity> findByNameIn(Collection<String> names);
}
