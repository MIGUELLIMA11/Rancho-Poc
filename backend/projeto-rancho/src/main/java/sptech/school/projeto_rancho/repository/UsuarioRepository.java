package sptech.school.projeto_rancho.repository;

import sptech.school.projeto_rancho.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Caminho: src/main/java/com/rancho/api/repository/UsuarioRepository.java
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
