package web.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import web.rbac.entity.UserRoleEntity;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRoleEntity, String> {
  List<UserRoleEntity> findByUserId(String userId);
}
