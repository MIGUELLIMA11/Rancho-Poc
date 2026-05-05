package sptech.school.projeto_rancho.repository;

import sptech.school.projeto_rancho.model.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Caminho: src/main/java/com/rancho/api/repository/EscalaRepository.java
 */
public interface EscalaRepository extends JpaRepository<Escala, Long> {

    List<Escala> findByDataBetween(LocalDate inicio, LocalDate fim);

    List<Escala> findByFreelancerId(Long freelancerId);

    List<Escala> findByStatus(String status);

    List<Escala> findByDataBetweenAndStatus(LocalDate inicio, LocalDate fim, String status);

    // Busca com múltiplos filtros opcionais
    @Query("SELECT e FROM Escala e WHERE " +
           "(:freelancerId IS NULL OR e.freelancer.id = :freelancerId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:dataInicio IS NULL OR e.data >= :dataInicio) AND " +
           "(:dataFim IS NULL OR e.data <= :dataFim) " +
           "ORDER BY e.data ASC, e.horaInicio ASC")
    List<Escala> buscarComFiltros(
        @Param("freelancerId") Long freelancerId,
        @Param("status") String status,
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    // Total financeiro de escalas confirmadas em um mês
    @Query("SELECT COALESCE(SUM(e.valorTotal), 0) FROM Escala e " +
           "WHERE e.status = 'confirmado' " +
           "AND YEAR(e.data) = :ano AND MONTH(e.data) = :mes")
    BigDecimal calcularTotalConfirmadoMes(@Param("ano") int ano, @Param("mes") int mes);

    // Total de escalas pendentes em um mês
    @Query("SELECT COALESCE(SUM(e.valorTotal), 0) FROM Escala e " +
           "WHERE e.status = 'pendente' " +
           "AND YEAR(e.data) = :ano AND MONTH(e.data) = :mes")
    BigDecimal calcularTotalPendenteMes(@Param("ano") int ano, @Param("mes") int mes);

    // Quantidade de escalas em um mês
    @Query("SELECT COUNT(e) FROM Escala e " +
           "WHERE YEAR(e.data) = :ano AND MONTH(e.data) = :mes")
    Long countEscalasMes(@Param("ano") int ano, @Param("mes") int mes);
}
