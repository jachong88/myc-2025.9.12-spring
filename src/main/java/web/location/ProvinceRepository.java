package web.location;

import org.springframework.data.jpa.repository.JpaRepository;
import web.location.entity.ProvinceEntity;

public interface ProvinceRepository extends JpaRepository<ProvinceEntity, String> {}