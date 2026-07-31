package py.com.servipy.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import py.com.servipy.user.domain.User;

import java.util.Optional;

/**
 * Repositorio de la entidad User. Vive en el slice propietario de la tabla
 * y es el único punto de acceso a usuarios para el resto del sistema.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByIdAndActiveTrue(Long id);

    boolean existsByEmailIgnoreCase(String email);
}
