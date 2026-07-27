package py.com.servipy.client.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import py.com.servipy.user.domain.User;

import java.util.Optional;

/**
 * Repositorio de lectura para la entidad User dentro del slice client.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndActiveTrue(Long id);
}
