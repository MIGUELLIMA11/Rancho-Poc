package sptech.school.projeto_rancho.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Caminho: src/main/java/com/rancho/api/dto/EscalaDTO.java
 */
public class EscalaDTO {

    private Long id;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @NotNull(message = "Freelancer é obrigatório")
    private Long freelancerId;

    // Campos de exibição — preenchidos pelo backend no response
    private String freelancerNome;
    private String freelancerEspecialidade;

    @NotNull(message = "Hora de início é obrigatória")
    private LocalTime horaInicio;

    @NotNull(message = "Hora de término é obrigatória")
    private LocalTime horaFim;

    private String funcao;

    private String status = "pendente"; // pendente | confirmado | cancelado

    private String observacoes;

    private BigDecimal valorTotal;

    private LocalDateTime criadoEm;

    // ── Getters e Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Long getFreelancerId() { return freelancerId; }
    public void setFreelancerId(Long freelancerId) { this.freelancerId = freelancerId; }

    public String getFreelancerNome() { return freelancerNome; }
    public void setFreelancerNome(String freelancerNome) { this.freelancerNome = freelancerNome; }

    public String getFreelancerEspecialidade() { return freelancerEspecialidade; }
    public void setFreelancerEspecialidade(String freelancerEspecialidade) {
        this.freelancerEspecialidade = freelancerEspecialidade;
    }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFim() { return horaFim; }
    public void setHoraFim(LocalTime horaFim) { this.horaFim = horaFim; }

    public String getFuncao() { return funcao; }
    public void setFuncao(String funcao) { this.funcao = funcao; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
