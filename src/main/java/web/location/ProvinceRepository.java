package web.location;

import org.springframework.data.jpa.repository.JpaRepository;
import web.location.entity.ProvinceEntity;

import java.util.Collection;
import java.util.List;

public interface ProvinceRepository extends JpaRepository<ProvinceEntity, String> {
  List<ProvinceEntity> findByCodeInIgnoreCase(Collection<String> codes);
}
