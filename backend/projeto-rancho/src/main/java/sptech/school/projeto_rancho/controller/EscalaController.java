package sptech.school.projeto_rancho.controller;

import sptech.school.projeto_rancho.dto.EscalaDTO;
import sptech.school.projeto_rancho.service.EscalaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ──────────────────────────────────────────────────────────
 *  RANCHO COMANCHE — EscalaController
 *  CRUD de escalas de trabalho.
 *
 *  Endpoints:
 *    GET    /api/escalas                → listar (com filtros)
 *    GET    /api/escalas/{id}           → buscar por ID
 *    POST   /api/escalas                → criar
 *    PUT    /api/escalas/{id}           → atualizar
 *    DELETE /api/escalas/{id}           → excluir
 *    GET    /api/escalas/mes/{ano}/{mes} → escalas do mês
 *    GET    /api/escalas/resumo         → totais financeiros
 * ──────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/escalas")
@CrossOrigin(origins = "*")
public class EscalaController {

    @Autowired
    private EscalaService escalaService;

    // ──────────────────────────────────────────────
    // GET /api/escalas
    // Parâmetros opcionais:
    //   ?freelancerId=1
    //   &status=confirmado
    //   &dataInicio=2025-01-01
    //   &dataFim=2025-01-31
    // ──────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<EscalaDTO>> listar(
            @RequestParam(required = false) Long freelancerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        List<EscalaDTO> lista = escalaService.listar(freelancerId, status, dataInicio, dataFim);
        return ResponseEntity.ok(lista);
    }

    // ──────────────────────────────────────────────
    // GET /api/escalas/{id}
    // ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return escalaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────
    // POST /api/escalas
    // Body: EscalaDTO
    // ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody EscalaDTO dto) {
        try {
            EscalaDTO criada = escalaService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(criada);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // PUT /api/escalas/{id}
    // Body: EscalaDTO
    // ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody EscalaDTO dto) {
        try {
            EscalaDTO atualizada = escalaService.atualizar(id, dto);
            return ResponseEntity.ok(atualizada);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // DELETE /api/escalas/{id}
    // ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            escalaService.excluir(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // GET /api/escalas/mes/{ano}/{mes}
    // Ex: /api/escalas/mes/2025/1 → Janeiro de 2025
    // ──────────────────────────────────────────────
    @GetMapping("/mes/{ano}/{mes}")
    public ResponseEntity<List<EscalaDTO>> porMes(
            @PathVariable int ano,
            @PathVariable int mes) {
        return ResponseEntity.ok(escalaService.listarPorMes(ano, mes));
    }

    // ──────────────────────────────────────────────
    // GET /api/escalas/resumo?mes=2025-01
    // Retorna totais financeiros do período
    // ──────────────────────────────────────────────
    @GetMapping("/resumo")
    public ResponseEntity<?> resumo(@RequestParam(required = false) String mes) {
        return ResponseEntity.ok(escalaService.calcularResumo(mes));
    }
}

/* ══════════════════════════════════════════════════════════
   DTO — EscalaDTO (criar em: com.rancho.api.dto)
   ══════════════════════════════════════════════════════════

public class EscalaDTO {
    private Long id;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @NotNull(message = "Freelancer é obrigatório")
    private Long freelancerId;

    // Para exibição (preenchido no response)
    private String freelancerNome;
    private String freelancerEspecialidade;

    @NotNull(message = "Hora de início é obrigatória")
    private LocalTime horaInicio;

    @NotNull(message = "Hora de término é obrigatória")
    private LocalTime horaFim;

    private String funcao;

    private String status = "pendente"; // pendente | confirmado | cancelado

    private String observacoes;

    @Column(precision = 10, scale = 2)
    private BigDecimal valorTotal;

    private LocalDateTime criadoEm;

    // getters e setters...
}

   ══════════════════════════════════════════════════════════
   MODEL — Escala (criar em: com.rancho.api.model)
   ══════════════════════════════════════════════════════════

@Entity
@Table(name = "escala")
public class Escala {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelancer_id", nullable = false)
    private Freelancer freelancer;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fim", nullable = false)
    private LocalTime horaFim;

    private String funcao;

    @Column(nullable = false, length = 15)
    private String status = "pendente";

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    // getters e setters...
}

   ══════════════════════════════════════════════════════════
   SQL — Tabelas de escala e pagamento
   ══════════════════════════════════════════════════════════

CREATE TABLE escala (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    data           DATE NOT NULL,
    freelancer_id  BIGINT NOT NULL,
    hora_inicio    TIME NOT NULL,
    hora_fim       TIME NOT NULL,
    funcao         VARCHAR(80),
    status         VARCHAR(15) NOT NULL DEFAULT 'pendente',
    observacoes    TEXT,
    valor_total    DECIMAL(10,2),
    criado_em      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (freelancer_id) REFERENCES freelancer(id) ON DELETE CASCADE,

    INDEX idx_data (data),
    INDEX idx_freelancer (freelancer_id),
    INDEX idx_status (status)
);

CREATE TABLE pagamento (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    escala_id        BIGINT NOT NULL UNIQUE,
    status_pagamento VARCHAR(15) NOT NULL DEFAULT 'pendente', -- pendente | pago
    data_pagamento   TIMESTAMP,
    criado_em        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (escala_id) REFERENCES escala(id) ON DELETE CASCADE
);

   ══════════════════════════════════════════════════════════
   SERVICE — EscalaService (resumo dos métodos)
   ══════════════════════════════════════════════════════════

@Service
public class EscalaService {

    @Autowired private EscalaRepository repo;
    @Autowired private FreelancerRepository freelancerRepo;

    public EscalaDTO criar(EscalaDTO dto) {
        Freelancer fl = freelancerRepo.findById(dto.getFreelancerId())
            .orElseThrow(() -> new RuntimeException("Freelancer não encontrado."));

        // Calcula valor total: horas * valorHora do freelancer
        Duration dur = Duration.between(dto.getHoraInicio(), dto.getHoraFim());
        double horas = dur.toMinutes() / 60.0;
        BigDecimal total = fl.getValorHora().multiply(BigDecimal.valueOf(horas));

        Escala e = new Escala();
        e.setData(dto.getData());
        e.setFreelancer(fl);
        e.setHoraInicio(dto.getHoraInicio());
        e.setHoraFim(dto.getHoraFim());
        e.setFuncao(dto.getFuncao());
        e.setStatus(dto.getStatus());
        e.setObservacoes(dto.getObservacoes());
        e.setValorTotal(total);

        return toDTO(repo.save(e));
    }

    public Map<String, Object> calcularResumo(String mes) {
        // Calcula totais financeiros do mês
        // Retorna: { totalConfirmado, totalPendente, quantidadeEscalas, ... }
    }
}

   ══════════════════════════════════════════════════════════
   REPOSITORY
   ══════════════════════════════════════════════════════════

public interface EscalaRepository extends JpaRepository<Escala, Long> {
    List<Escala> findByDataBetween(LocalDate inicio, LocalDate fim);
    List<Escala> findByFreelancerId(Long freelancerId);
    List<Escala> findByStatus(String status);
    List<Escala> findByDataBetweenAndStatus(LocalDate inicio, LocalDate fim, String status);

    @Query("SELECT SUM(e.valorTotal) FROM Escala e WHERE e.status = 'confirmado' AND YEAR(e.data) = :ano AND MONTH(e.data) = :mes")
    BigDecimal calcularTotalMes(@Param("ano") int ano, @Param("mes") int mes);
}
*/
