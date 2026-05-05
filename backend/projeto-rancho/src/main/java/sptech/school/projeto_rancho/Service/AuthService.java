package sptech.school.projeto_rancho.service;

import sptech.school.projeto_rancho.dto.auth.CadastroRequest;
import sptech.school.projeto_rancho.dto.auth.LoginResponse;
import sptech.school.projeto_rancho.dto.auth.UserDTO;
import sptech.school.projeto_rancho.model.Usuario;
import sptech.school.projeto_rancho.repository.UsuarioRepository;
import sptech.school.projeto_rancho.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * ──────────────────────────────────────────────────────────
 *  RANCHO COMANCHE — AuthService
 *  Lógica de autenticação: login, cadastro, recuperação de senha.
 *
 *  Caminho: com/rancho/api/service/AuthService.java
 * ──────────────────────────────────────────────────────────
 */
@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Armazena códigos de recuperação em memória (temporário)
    // Em produção use Redis ou tabela no banco
    private final Map<String, String[]> codigosRecuperacao = new HashMap<>();

    // ──────────────────────────────────────────────
    // LOGIN
    // POST /api/auth/login
    // Body: { "email": "...", "senha": "..." }
    // Returns: LoginResponse { token, user: { id, name, email, role } }
    // ──────────────────────────────────────────────
    public LoginResponse login(String email, String senha) {
        // 1. Busca o usuário pelo e-mail
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        // 2. Valida a senha com BCrypt
        if (!passwordEncoder.matches(senha, usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta.");
        }

        // 3. Gera o token JWT
        String token = jwtUtil.gerarToken(
            usuario.getEmail(),
            usuario.getId(),
            usuario.getRole()
        );

        // 4. Monta o UserDTO — ATENÇÃO: "name" é o campo esperado pelo frontend
        UserDTO userDTO = new UserDTO(
            usuario.getId(),
            usuario.getNome(),    // campo "nome" da entity → "name" no JSON
            usuario.getEmail(),
            usuario.getRole()
        );

        return new LoginResponse(token, userDTO);
    }

    // ──────────────────────────────────────────────
    // CADASTRO
    // POST /api/auth/cadastro
    // ──────────────────────────────────────────────
    public Usuario cadastrar(CadastroRequest req) {
        // Verifica se e-mail já existe
        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("E-mail já cadastrado no sistema.");
        }

        // Cria o usuário com senha encriptada
        Usuario usuario = new Usuario();
        usuario.setNome(req.getNome());
        usuario.setEmail(req.getEmail());
        usuario.setSenha(passwordEncoder.encode(req.getSenha()));
        usuario.setCpf(req.getCpf());
        usuario.setTelefone(req.getTelefone());
        usuario.setRole("GESTOR");

        // Endereço
        usuario.setCep(req.getCep());
        usuario.setLogradouro(req.getLogradouro());
        usuario.setNumero(req.getNumero());
        usuario.setComplemento(req.getComplemento());
        usuario.setBairro(req.getBairro());
        usuario.setCidade(req.getCidade());
        usuario.setEstado(req.getEstado());
        usuario.setLatitude(req.getLatitude());
        usuario.setLongitude(req.getLongitude());

        return usuarioRepository.save(usuario);
    }

    // ──────────────────────────────────────────────
    // RECUPERAR SENHA — Enviar código
    // POST /api/auth/recuperar-senha
    // ──────────────────────────────────────────────
    public void enviarCodigoRecuperacao(String email) {
        if (!usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("E-mail não encontrado no sistema.");
        }

        // Gera código de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(999999));
        // Expira em 15 minutos
        String expiracao = LocalDateTime.now().plusMinutes(15).toString();

        codigosRecuperacao.put(email, new String[]{codigo, expiracao});

        // TODO: enviar o código por e-mail via JavaMailSender
        // Por enquanto loga no console para testes
        System.out.println("========================================");
        System.out.println("CÓDIGO DE RECUPERAÇÃO PARA: " + email);
        System.out.println("CÓDIGO: " + codigo);
        System.out.println("Expira em: " + expiracao);
        System.out.println("========================================");
    }

    // ──────────────────────────────────────────────
    // VERIFICAR CÓDIGO
    // POST /api/auth/verificar-codigo
    // ──────────────────────────────────────────────
    public void verificarCodigo(String email, String codigo) {
        String[] dados = codigosRecuperacao.get(email);
        if (dados == null) {
            throw new RuntimeException("Nenhum código enviado para este e-mail.");
        }

        String codigoSalvo  = dados[0];
        String expiracao    = dados[1];

        if (LocalDateTime.now().isAfter(LocalDateTime.parse(expiracao))) {
            codigosRecuperacao.remove(email);
            throw new RuntimeException("Código expirado. Solicite um novo.");
        }

        if (!codigoSalvo.equals(codigo)) {
            throw new RuntimeException("Código inválido.");
        }
    }

    // ──────────────────────────────────────────────
    // REDEFINIR SENHA
    // POST /api/auth/redefinir-senha
    // ──────────────────────────────────────────────
    public void redefinirSenha(String email, String novaSenha) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        // Remove o código usado
        codigosRecuperacao.remove(email);
    }
}
