package web.user;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import web.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
  Optional<UserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
  Optional<UserEntity> findByPhoneAndDeletedAtIsNull(String phone);
  Optional<UserEntity> findByIdAndDeletedAtIsNull(String id);
  Page<UserEntity> findByDeletedAtIsNull(Pageable pageable);
}
