package py.com.servipy.city.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import py.com.servipy.city.domain.City;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {

    List<City> findAllByOrderByNameAsc();
}
