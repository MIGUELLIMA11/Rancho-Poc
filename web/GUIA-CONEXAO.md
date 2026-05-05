# 🤠 Rancho Comanche — Guia de Conexão Frontend ↔ Backend IntelliJ

## Visão Geral da Arquitetura

```
Browser (HTML/JS)                    IntelliJ IDEA (Spring Boot)
─────────────────                    ───────────────────────────
Src/index.html                       com.rancho.api
Src/cadastro.html   ──HTTP/REST──►   ├── controller/
Src/app.html                         │   ├── AuthController
js/config.js                         │   ├── FreelancerController
js/api-service.js                    │   ├── EscalaController
js/auth.js                           │   └── DistanciaController
                                     ├── service/
localhost:XXXX                       │   └── AuthService
(arquivo local ou                    ├── security/
 Live Server)          ◄──JSON────   │   ├── SecurityConfig ← CORS
                                     │   ├── JwtUtil
                     localhost:8080   │   └── JwtFilter
                                     ├── repository/
                                     └── dto/
```

---

## PASSO 1 — Estrutura do Projeto no IntelliJ

O projeto Spring Boot deve ter esta estrutura de pacotes:

```
src/main/
├── java/com/rancho/api/
│   ├── RanchoApiApplication.java       ← classe @SpringBootApplication
│   ├── config/
│   │   └── SecurityConfig.java         ← copiar de backend/SecurityConfig.java
│   ├── controller/
│   │   ├── AuthController.java         ← copiar de backend/AuthController.java
│   │   ├── FreelancerController.java   ← copiar de backend/FreelancerController.java
│   │   ├── EscalaController.java       ← copiar de backend/EscalaController.java
│   │   └── DistanciaController.java
│   ├── dto/
│   │   ├── auth/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── UserDTO.java
│   │   │   └── CadastroRequest.java
│   │   ├── FreelancerDTO.java
│   │   └── EscalaDTO.java
│   ├── model/
│   │   ├── Usuario.java
│   │   ├── Freelancer.java
│   │   └── Escala.java
│   ├── repository/
│   │   ├── UsuarioRepository.java
│   │   ├── FreelancerRepository.java
│   │   └── EscalaRepository.java
│   ├── security/
│   │   ├── JwtUtil.java               ← copiar de backend/JwtUtil.java
│   │   └── JwtFilter.java             ← copiar de backend/JwtFilter.java
│   └── service/
│       ├── AuthService.java            ← copiar de backend/AuthService.java
│       ├── FreelancerService.java
│       └── EscalaService.java
└── resources/
    └── application.properties          ← copiar de backend/application.properties
```

---

## PASSO 2 — Copiar Arquivos para o IntelliJ

Copie estes arquivos da pasta `backend/` para os locais corretos no projeto IntelliJ:

| Arquivo aqui                        | Destino no IntelliJ                                            |
|-------------------------------------|----------------------------------------------------------------|
| `backend/application.properties`    | `src/main/resources/application.properties`                   |
| `backend/SecurityConfig.java`       | `src/main/java/com/rancho/api/config/SecurityConfig.java`      |
| `backend/JwtUtil.java`              | `src/main/java/com/rancho/api/security/JwtUtil.java`           |
| `backend/JwtFilter.java`            | `src/main/java/com/rancho/api/security/JwtFilter.java`         |
| `backend/AuthService.java`          | `src/main/java/com/rancho/api/service/AuthService.java`        |
| `backend/AuthController.java`       | `src/main/java/com/rancho/api/controller/AuthController.java`  |
| `backend/FreelancerController.java` | `src/main/java/com/rancho/api/controller/FreelancerController.java` |
| `backend/EscalaController.java`     | `src/main/java/com/rancho/api/controller/EscalaController.java` |
| `backend/pom.xml`                   | `pom.xml` (raiz do projeto — mesclar dependências se já existir) |

---

## PASSO 3 — Configurar o application.properties

Edite `src/main/resources/application.properties`:

```properties
# 1. Troque a senha do MySQL
spring.datasource.password=SUA_SENHA_MYSQL

# 2. Certifique-se de que o banco existe:
#    Abra o MySQL Workbench e execute:
#    CREATE DATABASE rancho_comanche;

# 3. Troque o segredo JWT por algo aleatório longo
jwt.secret=RanchoComancheSecretKey2025SuperSegura!@#$%
```

> **Alternativa sem MySQL:** descomente as linhas do H2 no `application.properties`  
> para usar banco em memória (dados somem ao reiniciar).

---

## PASSO 4 — DTOs Necessários no IntelliJ

Crie estes DTOs que o frontend espera:

### `LoginRequest.java`
```java
package com.rancho.api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank private String email;
    @NotBlank private String senha;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
}
```

### `UserDTO.java`
```java
package com.rancho.api.dto.auth;

public class UserDTO {
    private Long id;
    private String name;   // ← "name" (não "nome") — frontend espera este campo
    private String email;
    private String role;

    public UserDTO(Long id, String name, String email, String role) {
        this.id = id; this.name = name; this.email = email; this.role = role;
    }
    // getters...
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
```

### `LoginResponse.java`
```java
package com.rancho.api.dto.auth;

public class LoginResponse {
    private String token;
    private UserDTO user;

    public LoginResponse(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }
    public String getToken() { return token; }
    public UserDTO getUser() { return user; }
}
```

### `UsuarioRepository.java`
```java
package com.rancho.api.repository;

import com.rancho.api.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

---

## PASSO 5 — Rodar o Backend no IntelliJ

1. Abra o projeto no IntelliJ IDEA
2. Aguarde o Maven baixar as dependências (barra de progresso na parte inferior)
3. Clique em **Run** na classe `RanchoApiApplication.java`  
   *(ou pressione `Shift+F10`)*
4. Aguarde a mensagem no console:
   ```
   Started RanchoApiApplication in X.XXX seconds
   ```
5. O backend estará disponível em: **http://localhost:8080**

---

## PASSO 6 — Testar a Conexão

### Teste 1: Verificar se o backend responde

Abra o navegador e acesse:
```
http://localhost:8080/api/auth/login
```
Deve retornar um erro 405 (Method Not Allowed) — isso significa que o servidor está **online** ✅

### Teste 2: Teste de Login via cURL ou Postman

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@rancho.com","senha":"123456"}'
```

Resposta esperada:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "Admin",
    "email": "admin@rancho.com",
    "role": "GESTOR"
  }
}
```

### Teste 3: Testar o Frontend

1. Abra `Src/index.html` no navegador (ou use Live Server no VS Code)
2. Tente fazer login — se o backend estiver rodando, o sistema usará os dados reais
3. Abra o Console do navegador (F12) e verifique:
   - `🤠 Rancho Comanche v1.0.0` → config.js carregado
   - `Backend: http://localhost:8080/api` → URL correta

---

## Mapeamento de Endpoints

| Frontend (js/api-service.js)          | Backend (IntelliJ)                    |
|---------------------------------------|---------------------------------------|
| `POST /api/auth/login`                | `AuthController.login()`              |
| `POST /api/auth/cadastro`             | `AuthController.cadastrar()`          |
| `POST /api/auth/recuperar-senha`      | `AuthController.recuperarSenha()`     |
| `GET  /api/freelancers`               | `FreelancerController.listar()`       |
| `POST /api/freelancers`               | `FreelancerController.criar()`        |
| `PUT  /api/freelancers/{id}`          | `FreelancerController.atualizar()`    |
| `DELETE /api/freelancers/{id}`        | `FreelancerController.excluir()`      |
| `GET  /api/escalas`                   | `EscalaController.listar()`           |
| `POST /api/escalas`                   | `EscalaController.criar()`            |
| `PUT  /api/escalas/{id}`              | `EscalaController.atualizar()`        |
| `DELETE /api/escalas/{id}`            | `EscalaController.excluir()`          |
| `GET  /api/pagamentos`                | *(implementar PagamentoController)*   |

---

## Problemas Comuns

### ❌ CORS Error no console do browser

**Causa:** Spring Security bloqueando o preflight OPTIONS antes do `@CrossOrigin` ser processado.  
**Solução:** Verifique se `SecurityConfig.java` está no projeto com a configuração CORS global.

### ❌ 401 Unauthorized em todas as requests

**Causa:** Token JWT não está sendo enviado ou é inválido.  
**Verificar:** No console do browser → Application → Local Storage → `rc_token` deve existir.

### ❌ 403 Forbidden no login

**Causa:** A rota `/api/auth/login` não foi adicionada aos endpoints públicos.  
**Solução:** Em `SecurityConfig.java`, verifique se `"/api/auth/login"` está em `PUBLIC_ENDPOINTS`.

### ❌ `Failed to fetch` no console (modo demo ativo)

**Causa:** Backend não está rodando.  
**Solução:** Inicie o projeto no IntelliJ. O modo demo continua funcionando como fallback.

### ❌ Senha não confere mas existe no banco

**Causa:** Senha salva sem BCrypt hash.  
**Solução:** Sempre use `passwordEncoder.encode(senha)` ao salvar usuários.

---

## Alterar a URL do Backend

Se o backend mudar de porta ou endereço, edite **apenas** `js/config.js`:

```javascript
const RC_CONFIG = {
  API_URL: 'http://localhost:8080/api',  // ← troque aqui
  // ...
};
```

Todos os outros arquivos (`api-service.js`, `auth.js`) lerão automaticamente.
