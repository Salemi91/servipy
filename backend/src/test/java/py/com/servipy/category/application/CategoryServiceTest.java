package py.com.servipy.category.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import py.com.servipy.category.application.dto.CategoryDto;
import py.com.servipy.category.domain.Category;
import py.com.servipy.category.infrastructure.persistence.CategoryRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void should_returnOnlyActiveCategories_when_listAll() {
        Category cat1 = new Category();
        cat1.setId(1L);
        cat1.setName("Electricidad");
        cat1.setIcon("bolt");
        cat1.setDescription("Servicios eléctricos");
        cat1.setActive(true);

        Category cat2 = new Category();
        cat2.setId(2L);
        cat2.setName("Plomería");
        cat2.setIcon("wrench");
        cat2.setDescription("Servicios de plomería");
        cat2.setActive(true);

        when(categoryRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(cat1, cat2));

        List<CategoryDto> result = categoryService.findAllActive();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Electricidad");
        assertThat(result.get(1).name()).isEqualTo("Plomería");
    }

    @Test
    void should_returnEmptyList_when_noCategoriesActive() {
        when(categoryRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of());

        List<CategoryDto> result = categoryService.findAllActive();

        assertThat(result).isEmpty();
    }
}
