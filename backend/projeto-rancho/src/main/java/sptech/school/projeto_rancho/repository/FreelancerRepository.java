package sptech.school.projeto_rancho.repository;

import sptech.school.projeto_rancho.model.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Caminho: src/main/java/com/rancho/api/repository/FreelancerRepository.java
 */
public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {

    boolean existsByCpf(String cpf);

    List<Freelancer> findByStatus(String status);

    List<Freelancer> findByDistanciaKmLessThanEqualAndStatus(double distanciaKm, String status);

    // Busca por nome, e-mail ou especialidade (case insensitive)
    @Query("SELECT f FROM Freelancer f WHERE " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:especialidade IS NULL OR f.especialidade = :especialidade) AND " +
           "(:search IS NULL OR " +
           "  LOWER(f.nome) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(f.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(f.especialidade) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Freelancer> buscarComFiltros(
        @Param("status") String status,
        @Param("especialidade") String especialidade,
        @Param("search") String search
    );
}
