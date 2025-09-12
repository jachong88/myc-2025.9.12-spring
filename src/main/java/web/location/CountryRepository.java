package web.location;

import org.springframework.data.jpa.repository.JpaRepository;
import web.location.entity.CountryEntity;

public interface CountryRepository extends JpaRepository<CountryEntity, String> {}