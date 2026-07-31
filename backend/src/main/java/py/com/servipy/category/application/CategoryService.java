package py.com.servipy.category.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.servipy.category.application.dto.CategoryDto;
import py.com.servipy.category.application.dto.CreateCategoryRequest;
import py.com.servipy.category.domain.Category;
import py.com.servipy.category.infrastructure.persistence.CategoryRepository;
import py.com.servipy.shared.exception.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Categoría activa por id, para que otros slices puedan referenciarla
     * sin acceder a su repositorio.
     */
    public Category findActiveEntityById(Long id) {
        return categoryRepository.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    public List<CategoryDto> findAllActive() {
        return categoryRepository.findByActiveTrueOrderByNameAsc().stream()
            .map(c -> new CategoryDto(c.getId(), c.getName(), c.getIcon(), c.getDescription()))
            .toList();
    }

    @Transactional
    public CategoryDto create(CreateCategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setIcon(request.icon());
        category.setDescription(request.description());
        category.setActive(true);
        Category saved = categoryRepository.save(category);
        return new CategoryDto(saved.getId(), saved.getName(), saved.getIcon(), saved.getDescription());
    }
}
