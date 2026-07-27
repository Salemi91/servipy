package py.com.servipy.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import py.com.servipy.user.domain.User;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad User.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
