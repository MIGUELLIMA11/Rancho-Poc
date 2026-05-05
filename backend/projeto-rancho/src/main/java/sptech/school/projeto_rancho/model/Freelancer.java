package sptech.school.projeto_rancho.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Caminho: src/main/java/com/rancho/api/model/Freelancer.java
 */
@Entity
@Table(name = "freelancer")
public class Freelancer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(unique = true, length = 14)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 80)
    private String especialidade;

    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorHora;

    @Column(nullable = false, length = 10)
    private String status = "ativo";

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    // ── Endereço ──
    @Column(nullable = false, length = 9)
    private String cep;
    private String logradouro;
    @Column(length = 20)
    private String numero;
    @Column(length = 100)
    private String complemento;
    @Column(length = 100)
    private String bairro;
    @Column(length = 100)
    private String cidade;
    @Column(length = 2)
    private String estado;

    // ── Coordenadas ──
    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double latitude;
    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double longitude;
    @Column(name = "distancia_km", columnDefinition = "DECIMAL(8,2)")
    private Double distanciaKm;

    // ── Dados Bancários / PIX (armazenados criptografados pelo frontend) ──
    @Column(name = "pix_chave", columnDefinition = "TEXT")
    private String pixChave;   // criptografado (AES-256 via frontend)

    @Column(name = "pix_tipo", length = 20)
    private String pixTipo;    // cpf | email | telefone | aleatoria

    @Column(length = 80)
    private String banco;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

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
