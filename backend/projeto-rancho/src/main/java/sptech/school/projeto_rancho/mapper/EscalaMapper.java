package sptech.school.projeto_rancho.mapper;

import sptech.school.projeto_rancho.dto.EscalaDTO;
import sptech.school.projeto_rancho.model.Escala;
import sptech.school.projeto_rancho.model.Freelancer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/mapper/EscalaMapper.java
 *
 * Converte entre a entidade Escala e o EscalaDTO.
 */
@Component
public class EscalaMapper {

    // ── Entity → DTO ──────────────────────────────────────────
    public EscalaDTO toDTO(Escala e) {
        if (e == null) return null;

        EscalaDTO dto = new EscalaDTO();
        dto.setId(e.getId());
        dto.setData(e.getData());
        dto.setHoraInicio(e.getHoraInicio());
        dto.setHoraFim(e.getHoraFim());
        dto.setFuncao(e.getFuncao());
        dto.setStatus(e.getStatus());
        dto.setObservacoes(e.getObservacoes());
        dto.setValorTotal(e.getValorTotal());
        dto.setCriadoEm(e.getCriadoEm());

        // Popula os dados do freelancer para exibição no frontend
        if (e.getFreelancer() != null) {
            dto.setFreelancerId(e.getFreelancer().getId());
            dto.setFreelancerNome(e.getFreelancer().getNome());
            dto.setFreelancerEspecialidade(e.getFreelancer().getEspecialidade());
        }

        return dto;
    }

    // ── DTO → Entity (novo) ───────────────────────────────────
    public Escala toEntity(EscalaDTO dto, Freelancer freelancer) {
        if (dto == null) return null;

        Escala e = new Escala();
        e.setData(dto.getData());
        e.setFreelancer(freelancer);
        e.setHoraInicio(dto.getHoraInicio());
        e.setHoraFim(dto.getHoraFim());
        e.setFuncao(dto.getFuncao());
        e.setStatus(dto.getStatus() != null ? dto.getStatus() : "pendente");
        e.setObservacoes(dto.getObservacoes());
        e.setCriadoEm(LocalDateTime.now());
        return e;
    }

    // ── Atualizar entidade existente com dados do DTO ─────────
    public void atualizarEntidade(Escala e, EscalaDTO dto, Freelancer freelancer) {
        e.setData(dto.getData());
        e.setFreelancer(freelancer);
        e.setHoraInicio(dto.getHoraInicio());
        e.setHoraFim(dto.getHoraFim());
        e.setFuncao(dto.getFuncao());
        if (dto.getStatus() != null) e.setStatus(dto.getStatus());
        e.setObservacoes(dto.getObservacoes());
    }
}
