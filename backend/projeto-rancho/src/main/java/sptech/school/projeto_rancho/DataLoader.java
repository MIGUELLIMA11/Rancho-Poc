package sptech.school.projeto_rancho;

import sptech.school.projeto_rancho.model.Usuario;
import sptech.school.projeto_rancho.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/DataLoader.java
 *
 * Cria um utilizador admin na inicialização (só se não existir).
 * Útil para testes com H2 (banco em memória).
 */
@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByEmail("admin@rancho.com")) {
                Usuario admin = new Usuario();
                admin.setNome("Admin Rancho");
                admin.setEmail("admin@rancho.com");
                admin.setSenha(encoder.encode("123456"));
                admin.setRole("GESTOR");
                repo.save(admin);

                System.out.println("========================================");
                System.out.println("✅ Utilizador criado:");
                System.out.println("   E-mail: admin@rancho.com");
                System.out.println("   Senha:  123456");
                System.out.println("========================================");
            }
        };
    }
}
