package web.location;

import org.springframework.data.jpa.repository.JpaRepository;
import web.location.entity.CountryEntity;

import java.util.Collection;
import java.util.List;

public interface CountryRepository extends JpaRepository<CountryEntity, String> {
  List<CountryEntity> findByCodeInIgnoreCase(Collection<String> codes);
}
