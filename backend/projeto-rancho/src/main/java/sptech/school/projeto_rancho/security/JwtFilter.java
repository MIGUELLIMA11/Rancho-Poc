package sptech.school.projeto_rancho.security;

import sptech.school.projeto_rancho.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * ──────────────────────────────────────────────────────────
 *  RANCHO COMANCHE — JwtFilter
 *  Intercepta TODAS as requests e valida o token JWT.
 *  Se o token for válido, autentica o usuário no contexto do Spring.
 *
 *  Caminho: com/rancho/api/security/JwtFilter.java
 * ──────────────────────────────────────────────────────────
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Pegar o cabeçalho Authorization
        String authHeader = request.getHeader("Authorization");

        // 2. Verificar se começa com "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extrair o token (remove "Bearer ")
        String token = authHeader.substring(7);

        // 4. Validar o token
        if (!jwtUtil.validarToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extrair informações do token
        String email = jwtUtil.extrairEmail(token);
        String role  = jwtUtil.extrairRole(token);

        // 6. Verificar se ainda não há autenticação no contexto
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 7. Cria o objeto de autenticação com a role do usuário
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "GESTOR"))
            );

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, authorities);

            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            // 8. Registra no contexto de segurança do Spring
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 9. Continua a cadeia de filtros
        filterChain.doFilter(request, response);
    }
}
