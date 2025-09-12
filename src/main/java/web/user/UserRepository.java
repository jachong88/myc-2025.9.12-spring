package web.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import web.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {
  Optional<UserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
  Optional<UserEntity> findByPhoneAndDeletedAtIsNull(String phone);
}
