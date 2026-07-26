package py.com.servipy.category.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.category.application.dto.CategoryDto;
import py.com.servipy.category.infrastructure.persistence.CategoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDto> findAllActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getIcon(), c.getDescription()))
                .toList();
    }
}
