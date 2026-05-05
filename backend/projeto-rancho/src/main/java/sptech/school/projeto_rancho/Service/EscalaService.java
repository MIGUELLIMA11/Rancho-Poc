package sptech.school.projeto_rancho.service;

import org.springframework.transaction.annotation.Transactional;
import sptech.school.projeto_rancho.dto.EscalaDTO;
import sptech.school.projeto_rancho.exception.RecursoNaoEncontradoException;
import sptech.school.projeto_rancho.mapper.EscalaMapper;
import sptech.school.projeto_rancho.model.Escala;
import sptech.school.projeto_rancho.model.Freelancer;
import sptech.school.projeto_rancho.repository.EscalaRepository;
import sptech.school.projeto_rancho.repository.FreelancerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/service/EscalaService.java
 */
@Service
public class EscalaService {

    @Autowired
    private EscalaRepository escalaRepo;

    @Autowired
    private FreelancerRepository freelancerRepo;

    @Autowired
    private EscalaMapper mapper;

    // ──────────────────────────────────────────────
    // Listar com filtros opcionais
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EscalaDTO> listar(Long freelancerId, String status,
                                   LocalDate dataInicio, LocalDate dataFim) {
        String st = (status != null && !status.isBlank()) ? status : null;
        return escalaRepo.buscarComFiltros(freelancerId, st, dataInicio, dataFim)
                         .stream()
                         .map(mapper::toDTO)
                         .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Buscar por ID
    // ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Optional<EscalaDTO> buscarPorId(Long id) {
        return escalaRepo.findById(id).map(mapper::toDTO);
    }

    // ──────────────────────────────────────────────
    // Criar nova escala
    // ──────────────────────────────────────────────
    public EscalaDTO criar(EscalaDTO dto) {
        Freelancer freelancer = freelancerRepo.findById(dto.getFreelancerId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Freelancer não encontrado. ID: " + dto.getFreelancerId()));

        Escala escala = mapper.toEntity(dto, freelancer);
        escala.setCriadoEm(LocalDateTime.now());

        // Calcula valor total: horas × valorHora do freelancer
        if (dto.getValorTotal() != null && dto.getValorTotal().compareTo(BigDecimal.ZERO) > 0) {
            escala.setValorTotal(dto.getValorTotal());
        } else {
            escala.setValorTotal(calcularValor(dto.getHoraInicio(), dto.getHoraFim(), freelancer.getValorHora()));
        }

        return mapper.toDTO(escalaRepo.save(escala));
    }

    // ──────────────────────────────────────────────
    // Atualizar escala
    // ──────────────────────────────────────────────
    public EscalaDTO atualizar(Long id, EscalaDTO dto) {
        Escala escala = escalaRepo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escala não encontrada. ID: " + id));

        Freelancer freelancer = freelancerRepo.findById(dto.getFreelancerId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Freelancer não encontrado. ID: " + dto.getFreelancerId()));

        mapper.atualizarEntidade(escala, dto, freelancer);

        if (dto.getValorTotal() != null && dto.getValorTotal().compareTo(BigDecimal.ZERO) > 0) {
            escala.setValorTotal(dto.getValorTotal());
        } else {
            escala.setValorTotal(calcularValor(dto.getHoraInicio(), dto.getHoraFim(), freelancer.getValorHora()));
        }

        return mapper.toDTO(escalaRepo.save(escala));
    }

    // ──────────────────────────────────────────────
    // Excluir escala
    // ──────────────────────────────────────────────
    public void excluir(Long id) {
        if (!escalaRepo.existsById(id)) {
            throw new RecursoNaoEncontradoException("Escala não encontrada. ID: " + id);
        }
        escalaRepo.deleteById(id);
    }

    // ──────────────────────────────────────────────
    // Listar escalas de um mês específico
    // ──────────────────────────────────────────────
    public List<EscalaDTO> listarPorMes(int ano, int mes) {
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim    = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return escalaRepo.findByDataBetween(inicio, fim)
                         .stream()
                         .map(mapper::toDTO)
                         .collect(Collectors.toList());
    }

    // ──────────────────────────────────────────────
    // Resumo financeiro do mês
    // ──────────────────────────────────────────────
    public Map<String, Object> calcularResumo(String mesParam) {
        int ano, mes;
        if (mesParam != null && mesParam.matches("\\d{4}-\\d{2}")) {
            String[] partes = mesParam.split("-");
            ano = Integer.parseInt(partes[0]);
            mes = Integer.parseInt(partes[1]);
        } else {
            LocalDate hoje = LocalDate.now();
            ano = hoje.getYear();
            mes = hoje.getMonthValue();
        }

        BigDecimal totalConfirmado = escalaRepo.calcularTotalConfirmadoMes(ano, mes);
        BigDecimal totalPendente   = escalaRepo.calcularTotalPendenteMes(ano, mes);
        Long       quantidade      = escalaRepo.countEscalasMes(ano, mes);

        Map<String, Object> resumo = new HashMap<>();
        resumo.put("mes",             String.format("%04d-%02d", ano, mes));
        resumo.put("totalConfirmado", totalConfirmado != null ? totalConfirmado : BigDecimal.ZERO);
        resumo.put("totalPendente",   totalPendente   != null ? totalPendente   : BigDecimal.ZERO);
        resumo.put("totalGeral",      (totalConfirmado != null ? totalConfirmado : BigDecimal.ZERO)
                                      .add(totalPendente != null ? totalPendente : BigDecimal.ZERO));
        resumo.put("quantidadeEscalas", quantidade != null ? quantidade : 0L);
        return resumo;
    }

    // ──────────────────────────────────────────────
    // Calcula valor: horas × valorHora
    // ──────────────────────────────────────────────
    private BigDecimal calcularValor(LocalTime inicio, LocalTime fim, BigDecimal valorHora) {
        if (inicio == null || fim == null || valorHora == null) return BigDecimal.ZERO;
        long minutos = Duration.between(inicio, fim).toMinutes();
        if (minutos <= 0) return BigDecimal.ZERO;
        BigDecimal horas = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return valorHora.multiply(horas).setScale(2, RoundingMode.HALF_UP);
    }
}
