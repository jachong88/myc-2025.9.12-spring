package web.rbac;

import org.springframework.data.jpa.repository.JpaRepository;
import web.rbac.entity.UserScopeEntity;

import java.util.List;

public interface UserScopeRepository extends JpaRepository<UserScopeEntity, String> {
  List<UserScopeEntity> findByUserId(String userId);
}
