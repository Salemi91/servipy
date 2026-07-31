package py.com.servipy.city.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.city.application.dto.CityDto;
import py.com.servipy.city.domain.City;
import py.com.servipy.city.infrastructure.persistence.CityRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<CityDto> findAll() {
        return cityRepository.findAllByOrderByNameAsc().stream()
            .map(c -> new CityDto(c.getId(), c.getName()))
            .toList();
    }

    /**
     * Ciudad por id, para que otros slices puedan referenciarla
     * sin acceder a su repositorio.
     */
    public City findEntityById(Long id) {
        return cityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ciudad no encontrada con id: " + id));
    }
}
