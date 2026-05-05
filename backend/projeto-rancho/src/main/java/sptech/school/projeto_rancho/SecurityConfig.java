package sptech.school.projeto_rancho.config;

import sptech.school.projeto_rancho.security.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ──────────────────────────────────────────────────────────
 *  RANCHO COMANCHE — SecurityConfig
 *
 *  IMPORTANTE: Esta configuração é obrigatória para o CORS
 *  funcionar com Spring Security. O @CrossOrigin nos Controllers
 *  sozinho NÃO é suficiente quando há Spring Security ativo.
 *
 *  Caminho: com/rancho/api/config/SecurityConfig.java
 * ──────────────────────────────────────────────────────────
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    /**
     * URL exata do frontend em produção.
     * Configure via variável de ambiente APP_FRONTEND_URL no Render/Railway.
     * Exemplo: https://seu-usuario.github.io/RanchoApi
     */
    @Value("${app.frontend-url:}")
    private String frontendUrl;

    // ──────────────────────────────────────────────
    // Endpoints PÚBLICOS (sem JWT)
    // ──────────────────────────────────────────────
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/api/auth/cadastro",
        "/api/auth/recuperar-senha",
        "/api/auth/verificar-codigo",
        "/api/auth/redefinir-senha",
        "/h2-console/**",   // só dev
        "/actuator/**"      // monitoramento
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Habilita CORS com a configuração abaixo
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 2. Desabilita CSRF (APIs REST stateless não precisam)
            .csrf(csrf -> csrf.disable())

            // 3. Sessão stateless (JWT cuida da autenticação)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. Autorização de endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // preflight CORS
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                .anyRequest().authenticated()
            )

            // 5. Adiciona o filtro JWT antes do filtro padrão de login
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Permite frames (para H2 Console em dev)
        http.headers(headers -> headers.frameOptions(f -> f.disable()));

        return http.build();
    }

    // ──────────────────────────────────────────────
    // Configuração CORS global
    // Permite que o browser chame o backend de qualquer origem local
    // ──────────────────────────────────────────────
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Origens permitidas: localhost (dev) + principais plataformas de hospedagem
        List<String> origens = new ArrayList<>(List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "null",                       // file:// (abrir HTML direto do disco)
            "https://*.github.io",        // GitHub Pages
            "https://*.onrender.com",     // Render
            "https://*.railway.app",      // Railway
            "https://*.netlify.app",      // Netlify
            "https://*.vercel.app"        // Vercel
        ));
        // URL exata configurada via env var APP_FRONTEND_URL (maior prioridade)
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            origens.add(frontendUrl);
        }
        config.setAllowedOriginPatterns(origens);

        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Cabeçalhos permitidos
        config.setAllowedHeaders(Arrays.asList("*"));

        // Permite envio de cookies/credenciais
        config.setAllowCredentials(true);

        // Cache do preflight por 1 hora
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ──────────────────────────────────────────────
    // BCrypt para hash de senhas
    // ──────────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ──────────────────────────────────────────────
    // AuthenticationManager para injeção no AuthService
    // ──────────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
