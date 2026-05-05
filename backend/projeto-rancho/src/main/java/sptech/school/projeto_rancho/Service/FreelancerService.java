package sptech.school.projeto_rancho.service;

import sptech.school.projeto_rancho.dto.FreelancerDTO;
import sptech.school.projeto_rancho.exception.RecursoNaoEncontradoException;
import sptech.school.projeto_rancho.mapper.FreelancerMapper;
import sptech.school.projeto_rancho.model.Freelancer;
import sptech.school.projeto_rancho.repository.FreelancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/service/FreelancerService.java
 */
@Service
public class FreelancerService {

    @Autowired
    private FreelancerRepository repo;

    @Autowired
    private FreelancerMapper mapper;

    // ──────────────────────────────────────────────
    // Listar com filtros opcionais
    // ──────────────────────────────────────────────
    public List<FreelancerDTO> listar(String status, String especialidade, String search) {
        String st  = (status        != null && !status.isBlank())        ? status        : null;
        String esp = (especialidade != null && !especialidade.isBlank())  ? especialidade : null;
        String sr  = (search        != null && !search.isBlank())         ? search        : null;

        return repo.buscarComFiltros(st, esp, sr)
                   .stream()
                   .map(mapper::toDTO)
                   .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Buscar por ID
    // ──────────────────────────────────────────────
    public Optional<FreelancerDTO> buscarPorId(Long id) {
        return repo.findById(id).map(mapper::toDTO);
    }

    // ──────────────────────────────────────────────
    // Criar
    // ──────────────────────────────────────────────
    public FreelancerDTO criar(FreelancerDTO dto) {
        if (dto.getCpf() != null && !dto.getCpf().isBlank()
                && repo.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado no sistema.");
        }

        Freelancer f = mapper.toEntity(dto);
        f.setCriadoEm(LocalDateTime.now());
        return mapper.toDTO(repo.save(f));
    }

    // ──────────────────────────────────────────────
    // Atualizar
    // ──────────────────────────────────────────────
    public FreelancerDTO atualizar(Long id, FreelancerDTO dto) {
        Freelancer f = repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Freelancer não encontrado. ID: " + id));

        if (dto.getCpf() != null && !dto.getCpf().isBlank()
                && !dto.getCpf().equals(f.getCpf())
                && repo.existsByCpf(dto.getCpf())) {
            throw new RuntimeException("CPF já cadastrado para outro freelancer.");
        }

        mapper.atualizarEntidade(f, dto);
        return mapper.toDTO(repo.save(f));
    }

    // ──────────────────────────────────────────────
    // Excluir
    // ──────────────────────────────────────────────
    public void excluir(Long id) {
        if (!repo.existsById(id)) {
            throw new RecursoNaoEncontradoException("Freelancer não encontrado. ID: " + id);
        }
        repo.deleteById(id);
    }

    // ──────────────────────────────────────────────
    // Listar por distância (raio em km)
    // ──────────────────────────────────────────────
    public List<FreelancerDTO> listarPorDistancia(double raioKm) {
        return repo.findByDistanciaKmLessThanEqualAndStatus(raioKm, "ativo")
                   .stream()
                   .map(mapper::toDTO)
                   .collect(Collectors.toList());
    }
}
