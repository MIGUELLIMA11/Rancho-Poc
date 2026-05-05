package sptech.school.projeto_rancho.mapper;

import sptech.school.projeto_rancho.dto.FreelancerDTO;
import sptech.school.projeto_rancho.model.Freelancer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/mapper/FreelancerMapper.java
 *
 * Converte entre a entidade Freelancer e o FreelancerDTO.
 * Mantém os Services e Controllers limpos.
 */
@Component
public class FreelancerMapper {

    // ── Entity → DTO ──────────────────────────────────────────
    public FreelancerDTO toDTO(Freelancer f) {
        if (f == null) return null;

        FreelancerDTO dto = new FreelancerDTO();
        dto.setId(f.getId());
        dto.setNome(f.getNome());
        dto.setCpf(f.getCpf());
        dto.setTelefone(f.getTelefone());
        dto.setEmail(f.getEmail());
        dto.setEspecialidade(f.getEspecialidade());
        dto.setValorHora(f.getValorHora());
        dto.setStatus(f.getStatus());
        dto.setObservacoes(f.getObservacoes());
        dto.setCep(f.getCep());
        dto.setLogradouro(f.getLogradouro());
        dto.setNumero(f.getNumero());
        dto.setComplemento(f.getComplemento());
        dto.setBairro(f.getBairro());
        dto.setCidade(f.getCidade());
        dto.setEstado(f.getEstado());
        dto.setLatitude(f.getLatitude());
        dto.setLongitude(f.getLongitude());
        dto.setDistanciaKm(f.getDistanciaKm());
        dto.setPixChave(f.getPixChave());
        dto.setPixTipo(f.getPixTipo());
        dto.setBanco(f.getBanco());
        dto.setCriadoEm(f.getCriadoEm());
        return dto;
    }

    // ── DTO → Entity (novo) ───────────────────────────────────
    public Freelancer toEntity(FreelancerDTO dto) {
        if (dto == null) return null;

        Freelancer f = new Freelancer();
        f.setNome(dto.getNome());
        f.setCpf(dto.getCpf());
        f.setTelefone(dto.getTelefone());
        f.setEmail(dto.getEmail());
        f.setEspecialidade(dto.getEspecialidade());
        f.setValorHora(dto.getValorHora());
        f.setStatus(dto.getStatus() != null ? dto.getStatus() : "ativo");
        f.setObservacoes(dto.getObservacoes());
        f.setCep(dto.getCep());
        f.setLogradouro(dto.getLogradouro());
        f.setNumero(dto.getNumero());
        f.setComplemento(dto.getComplemento());
        f.setBairro(dto.getBairro());
        f.setCidade(dto.getCidade());
        f.setEstado(dto.getEstado());
        f.setLatitude(dto.getLatitude());
        f.setLongitude(dto.getLongitude());
        f.setDistanciaKm(dto.getDistanciaKm());
        f.setPixChave(dto.getPixChave());
        f.setPixTipo(dto.getPixTipo());
        f.setBanco(dto.getBanco());
        f.setCriadoEm(LocalDateTime.now());
        return f;
    }

    // ── Atualizar entidade existente com dados do DTO ─────────
    public void atualizarEntidade(Freelancer f, FreelancerDTO dto) {
        f.setNome(dto.getNome());
        f.setCpf(dto.getCpf());
        f.setTelefone(dto.getTelefone());
        f.setEmail(dto.getEmail());
        f.setEspecialidade(dto.getEspecialidade());
        f.setValorHora(dto.getValorHora());
        if (dto.getStatus() != null) f.setStatus(dto.getStatus());
        f.setObservacoes(dto.getObservacoes());
        f.setCep(dto.getCep());
        f.setLogradouro(dto.getLogradouro());
        f.setNumero(dto.getNumero());
        f.setComplemento(dto.getComplemento());
        f.setBairro(dto.getBairro());
        f.setCidade(dto.getCidade());
        f.setEstado(dto.getEstado());
        f.setLatitude(dto.getLatitude());
        f.setLongitude(dto.getLongitude());
        f.setDistanciaKm(dto.getDistanciaKm());
        if (dto.getPixChave() != null) f.setPixChave(dto.getPixChave());
        if (dto.getPixTipo() != null) f.setPixTipo(dto.getPixTipo());
        if (dto.getBanco()   != null) f.setBanco(dto.getBanco());
    }
}
