package sptech.school.projeto_rancho.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ──────────────────────────────────────────────────────────
 *  RANCHO COMANCHE — JwtUtil
 *  Gera e valida tokens JWT.
 *
 *  Dependência necessária no pom.xml:
 *    <dependency>
 *      <groupId>io.jsonwebtoken</groupId>
 *      <artifactId>jjwt-api</artifactId>
 *      <version>0.11.5</version>
 *    </dependency>
 *    <dependency>
 *      <groupId>io.jsonwebtoken</groupId>
 *      <artifactId>jjwt-impl</artifactId>
 *      <version>0.11.5</version>
 *      <scope>runtime</scope>
 *    </dependency>
 *    <dependency>
 *      <groupId>io.jsonwebtoken</groupId>
 *      <artifactId>jjwt-jackson</artifactId>
 *      <version>0.11.5</version>
 *      <scope>runtime</scope>
 *    </dependency>
 *
 *  Caminho: com/rancho/api/security/JwtUtil.java
 * ──────────────────────────────────────────────────────────
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration; // ms (default: 86400000 = 24h)

    // ──────────────────────────────────────────────
    // Gera chave HMAC a partir do secret
    // ──────────────────────────────────────────────
    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ──────────────────────────────────────────────
    // Gerar token JWT para um usuário
    // ──────────────────────────────────────────────
    public String gerarToken(String email, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ──────────────────────────────────────────────
    // Extrair e-mail (subject) do token
    // ──────────────────────────────────────────────
    public String extrairEmail(String token) {
        return getClaims(token).getSubject();
    }

    // ──────────────────────────────────────────────
    // Extrair ID do usuário
    // ──────────────────────────────────────────────
    public Long extrairUserId(String token) {
        Object id = getClaims(token).get("userId");
        if (id instanceof Integer) return ((Integer) id).longValue();
        return (Long) id;
    }

    // ──────────────────────────────────────────────
    // Extrair role
    // ──────────────────────────────────────────────
    public String extrairRole(String token) {
        return (String) getClaims(token).get("role");
    }

    // ──────────────────────────────────────────────
    // Validar token (assinatura + expiração)
    // ──────────────────────────────────────────────
    public boolean validarToken(String token) {
        try {
            getClaims(token);
            return !isExpirado(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ──────────────────────────────────────────────
    // Verificar se o token está expirado
    // ──────────────────────────────────────────────
    public boolean isExpirado(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // ──────────────────────────────────────────────
    // Parsear claims (payload) do token
    // ──────────────────────────────────────────────
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
