package py.com.servipy.category.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import py.com.servipy.category.domain.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByNameAsc();
}
