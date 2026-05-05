package sptech.school.projeto_rancho.controller;

import sptech.school.projeto_rancho.dto.auth.*;
import sptech.school.projeto_rancho.model.Usuario;
import sptech.school.projeto_rancho.repository.UsuarioRepository;
import sptech.school.projeto_rancho.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ──────────────────────────────────────────────────────────
 *  RANCHO COMANCHE — AuthController
 *  Endpoints de autenticação:
 *    POST /api/auth/login
 *    POST /api/auth/cadastro
 *    POST /api/auth/recuperar-senha
 *    POST /api/auth/verificar-codigo
 *    POST /api/auth/redefinir-senha
 * ──────────────────────────────────────────────────────────
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ──────────────────────────────────────────────
    // POST /api/auth/login
    // Body: { "email": "...", "senha": "..." }
    // Returns: { "token": "...", "user": { ... } }
    // ──────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            LoginResponse response = authService.login(req.getEmail(), req.getSenha());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Usuário ou senha incorretos."));
        }
    }

    // ──────────────────────────────────────────────
    // POST /api/auth/cadastro
    // Body: CadastroRequest (com todos os dados do usuário + endereço)
    // Returns: { "id": ..., "email": "..." }
    // ──────────────────────────────────────────────
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@Valid @RequestBody CadastroRequest req) {
        try {
            Usuario usuario = authService.cadastrar(req);
            return ResponseEntity.ok(Map.of(
                "id",      usuario.getId(),
                "email",   usuario.getEmail(),
                "message", "Conta criada com sucesso!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // POST /api/auth/recuperar-senha
    // Body: { "email": "..." }
    // Envia código de recuperação por e-mail
    // ──────────────────────────────────────────────
    @PostMapping("/recuperar-senha")
    public ResponseEntity<?> recuperarSenha(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        try {
            authService.enviarCodigoRecuperacao(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Código enviado para " + email
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────
    // POST /api/auth/verificar-codigo
    // Body: { "email": "...", "codigo": "123456" }
    // ──────────────────────────────────────────────
    @PostMapping("/verificar-codigo")
    public ResponseEntity<?> verificarCodigo(@RequestBody Map<String, String> body) {
        String email  = body.get("email");
        String codigo = body.get("codigo");
        try {
            authService.verificarCodigo(email, codigo);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Código inválido ou expirado."));
        }
    }

    // ──────────────────────────────────────────────
    // POST /api/auth/redefinir-senha
    // Body: { "email": "...", "novaSenha": "..." }
    // ──────────────────────────────────────────────
    @PostMapping("/redefinir-senha")
    public ResponseEntity<?> redefinirSenha(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String novaSenha = body.get("novaSenha");
        try {
            authService.redefinirSenha(email, novaSenha);
            return ResponseEntity.ok(Map.of("success", true, "message", "Senha redefinida com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}

/* ══════════════════════════════════════════════════════════
   DTOs (criar em: com.rancho.api.dto.auth)
   ══════════════════════════════════════════════════════════

// LoginRequest.java
public class LoginRequest {
    @NotBlank private String email;
    @NotBlank private String senha;
    // getters e setters
}

// LoginResponse.java
public class LoginResponse {
    private String token;
    private UserDTO user;
    // constructor, getters
}

// UserDTO.java
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String role;
}

// CadastroRequest.java
public class CadastroRequest {
    @NotBlank private String nome;
    @Email private String email;
    private String cpf;
    private String telefone;
    @NotBlank @Size(min=6) private String senha;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private Double latitude;
    private Double longitude;
    // getters e setters
}

   ══════════════════════════════════════════════════════════
   MODEL — Usuario (criar em: com.rancho.api.model)
   ══════════════════════════════════════════════════════════

@Entity
@Table(name = "usuario")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha; // BCrypt hash

    private String cpf;
    private String telefone;
    private String role = "GESTOR";

    // Endereço
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;

    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double latitude;

    @Column(columnDefinition = "DECIMAL(10,7)")
    private Double longitude;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();

    // getters e setters...
}

   ══════════════════════════════════════════════════════════
   SQL — Tabela de usuários
   ══════════════════════════════════════════════════════════

CREATE TABLE usuario (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    senha       VARCHAR(255) NOT NULL,
    cpf         VARCHAR(14),
    telefone    VARCHAR(20),
    role        VARCHAR(30) DEFAULT 'GESTOR',
    cep         VARCHAR(9),
    logradouro  VARCHAR(255),
    numero      VARCHAR(20),
    complemento VARCHAR(100),
    bairro      VARCHAR(100),
    cidade      VARCHAR(100),
    estado      CHAR(2),
    latitude    DECIMAL(10,7),
    longitude   DECIMAL(10,7),
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE codigo_recuperacao (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(150) NOT NULL,
    codigo      VARCHAR(6) NOT NULL,
    expira_em   TIMESTAMP NOT NULL,
    usado       BOOLEAN DEFAULT FALSE,
    criado_em   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
*/
