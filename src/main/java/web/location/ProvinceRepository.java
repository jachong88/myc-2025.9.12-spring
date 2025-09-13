package web.location;

import org.springframework.data.jpa.repository.JpaRepository;
import web.location.entity.ProvinceEntity;

import java.util.Collection;
import java.util.List;

public interface ProvinceRepository extends JpaRepository<ProvinceEntity, String> {
  List<ProvinceEntity> findByProvinceCodeInIgnoreCase(Collection<String> provinceCodes);
  List<ProvinceEntity> findByCountryCodeOrderByName(String countryCode);
  List<ProvinceEntity> findByCountryCodeAndProvinceCodeInIgnoreCase(String countryCode, Collection<String> provinceCodes);
}
