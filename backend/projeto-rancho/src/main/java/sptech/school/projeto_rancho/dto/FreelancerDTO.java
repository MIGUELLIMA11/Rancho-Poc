package sptech.school.projeto_rancho.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Caminho: src/main/java/com/rancho/api/dto/FreelancerDTO.java
 */
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
    @DecimalMin(value = "0.01", message = "Valor/hora deve ser maior que zero")
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

    // Coordenadas (calculadas pelo frontend via Nominatim/Haversine)
    private Double latitude;
    private Double longitude;
    private Double distanciaKm;

    // Dados Bancários / PIX (armazenados criptografados pelo frontend)
    private String pixChave;   // campo AES-256-GCM — não logar em produção
    private String pixTipo;    // cpf | email | telefone | aleatoria
    private String banco;

    private LocalDateTime criadoEm;

    // ── Getters e Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public BigDecimal getValorHora() { return valorHora; }
    public void setValorHora(BigDecimal valorHora) { this.valorHora = valorHora; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getDistanciaKm() { return distanciaKm; }
    public void setDistanciaKm(Double distanciaKm) { this.distanciaKm = distanciaKm; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public String getPixChave() { return pixChave; }
    public void setPixChave(String pixChave) { this.pixChave = pixChave; }

    public String getPixTipo() { return pixTipo; }
    public void setPixTipo(String pixTipo) { this.pixTipo = pixTipo; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }
}
