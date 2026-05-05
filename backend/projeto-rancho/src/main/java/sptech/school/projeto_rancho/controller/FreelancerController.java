package sptech.school.projeto_rancho.controller;

import sptech.school.projeto_rancho.dto.FreelancerDTO;
import sptech.school.projeto_rancho.model.Freelancer;
import sptech.school.projeto_rancho.repository.FreelancerRepository;
import sptech.school.projeto_rancho.service.FreelancerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ──────────────────────────────────────────────────────────
 *  RANCHO COMANCHE — FreelancerController
 *  CRUD completo de freelancers.
 *
 *  Endpoints:
 *    GET    /api/freelancers          → listar todos
 *    GET    /api/freelancers/{id}     → buscar por ID
 *    POST   /api/freelancers          → criar
 *    PUT    /api/freelancers/{id}     → atualizar
 *    DELETE /api/freelancers/{id}     → excluir
 *    GET    /api/freelancers/proximos → listar por distância
 * ──────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/freelancers")
@CrossOrigin(origins = "*")
public class FreelancerController {

    @Autowired
    private FreelancerService freelancerService;

    // ──────────────────────────────────────────────
    // GET /api/freelancers
    // Parâmetros opcionais: ?status=ativo&especialidade=Garçom&search=nome
    // ──────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<FreelancerDTO>> listar(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String especialidade,
            @RequestParam(required = false) String search) {

        List<FreelancerDTO> lista = freelancerService.listar(status, especialidade, search);
        return ResponseEntity.ok(lista);
    }

    // ──────────────────────────────────────────────
    // GET /api/freelancers/{id}
    // ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        return freelancerService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────
    // POST /api/freelancers
    // Body: FreelancerDTO (com endereço via CEP)
    // ──────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody FreelancerDTO dto) {
        try {
            FreelancerDTO criado = freelancerService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // PUT /api/freelancers/{id}
    // Body: FreelancerDTO
    // ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody FreelancerDTO dto) {
        try {
            FreelancerDTO atualizado = freelancerService.atualizar(id, dto);
            return ResponseEntity.ok(atualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // DELETE /api/freelancers/{id}
    // ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            freelancerService.excluir(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Freelancer excluído."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // GET /api/freelancers/proximos?raioKm=15
    // Lista freelancers dentro de um raio do restaurante
    // ──────────────────────────────────────────────
    @GetMapping("/proximos")
    public ResponseEntity<List<FreelancerDTO>> proximos(
            @RequestParam(defaultValue = "15") double raioKm) {
        return ResponseEntity.ok(freelancerService.listarPorDistancia(raioKm));
    }
}

/* ══════════════════════════════════════════════════════════
   DTO — FreelancerDTO (criar em: com.rancho.api.dto)
   ══════════════════════════════════════════════════════════

public class FreelancerDTO {
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String cpf;

    @NotBlank(message = "Telefone é obrigatório")
    private String telefone;

    private String email;

    @NotBlank(message = "Especialidade é obrigatória")
    private String especialidade;

    @NotNull(message = "Valor/hora é obrigatório")
    @DecimalMin("0.01")
    private BigDecimal valorHora;

    private String status = "ativo";
    private String observacoes;

    // Endereço
    @NotBlank(message = "CEP é obrigatório")
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    // Coordenadas (preenchidas pelo frontend via Nominatim)
    private Double latitude;
    private Double longitude;
    private Double distanciaKm;

    private LocalDateTime criadoEm;

    // getters e setters...
}

   ══════════════════════════════════════════════════════════
   MODEL — Freelancer (criar em: com.rancho.api.model)
   ══════════════════════════════════════════════════════════

@Entity
@Table(name = "freelancer")
public class Freelancer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true)
    private String cpf;

    @Column(nullable = false)
    private String telefone;

    private String email;

    @Column(nullable = false)
    private String especialidade;

    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    @Column(nullable = false, length = 10)
    private String status = "ativo";

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // Endereço
    @Column(nullable = false, length = 9)
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    @Column(length = 2)
    private String estado;

    // Coordenadas geográficas
    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double latitude;
    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double longitude;
    @Column(name = "distancia_km", columnDefinition = "DECIMAL(8,2)")
    private Double distanciaKm;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    // getters e setters...
}

   ══════════════════════════════════════════════════════════
   SQL — Tabela de freelancers
   ══════════════════════════════════════════════════════════

CREATE TABLE freelancer (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome         VARCHAR(150) NOT NULL,
    cpf          VARCHAR(14) UNIQUE,
    telefone     VARCHAR(20) NOT NULL,
    email        VARCHAR(150),
    especialidade VARCHAR(80) NOT NULL,
    valor_hora   DECIMAL(10,2) NOT NULL,
    status       VARCHAR(10) NOT NULL DEFAULT 'ativo',
    observacoes  TEXT,

    -- Endereço (preenchido via CEP → ViaCEP)
    cep          VARCHAR(9) NOT NULL,
    logradouro   VARCHAR(255),
    numero       VARCHAR(20),
    complemento  VARCHAR(100),
    bairro       VARCHAR(100),
    cidade       VARCHAR(100),
    estado       CHAR(2),

    -- Localização geográfica (via Nominatim)
    latitude     DECIMAL(10,7),
    longitude    DECIMAL(10,7),
    distancia_km DECIMAL(8,2),   -- distância ao restaurante em KM

    criado_em    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_status (status),
    INDEX idx_especialidade (especialidade),
    INDEX idx_cidade (cidade)
);

   ══════════════════════════════════════════════════════════
   SERVICE — FreelancerService (resumo)
   ══════════════════════════════════════════════════════════

@Service
public class FreelancerService {

    @Autowired
    private FreelancerRepository repo;

    public List<FreelancerDTO> listar(String status, String especialidade, String search) {
        // Implementar filtros com JPA Specifications ou queries customizadas
        return repo.findAll(buildSpec(status, especialidade, search))
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public FreelancerDTO criar(FreelancerDTO dto) {
        // Validar CPF único
        if (dto.getCpf() != null && repo.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado.");
        }
        Freelancer f = toEntity(dto);
        return toDTO(repo.save(f));
    }

    public FreelancerDTO atualizar(Long id, FreelancerDTO dto) {
        Freelancer f = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Freelancer não encontrado."));
        // Atualizar campos...
        return toDTO(repo.save(f));
    }

    public void excluir(Long id) {
        if (!repo.existsById(id)) throw new RuntimeException("Freelancer não encontrado.");
        repo.deleteById(id);
    }

    public List<FreelancerDTO> listarPorDistancia(double raioKm) {
        return repo.findByDistanciaKmLessThanEqualAndStatus(raioKm, "ativo")
                   .stream().map(this::toDTO).collect(Collectors.toList());
    }
}

   ══════════════════════════════════════════════════════════
   REPOSITORY
   ══════════════════════════════════════════════════════════

public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {
    boolean existsByCpf(String cpf);
    List<Freelancer> findByDistanciaKmLessThanEqualAndStatus(double distanciaKm, String status);
    List<Freelancer> findByStatus(String status);
}
*/
