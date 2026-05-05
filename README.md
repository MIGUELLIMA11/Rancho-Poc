# 🤠 Rancho Comanche — Sistema de Gestão de Freelancers

> POC (Prova de Conceito) de um sistema completo para gestão de freelancers, escalas e pagamentos de um restaurante. Desenvolvido com frontend em HTML/CSS/JS puro e backend em Java Spring Boot.

---

## 📋 Índice

- [Objetivo](#-objetivo)
- [Funcionalidades](#-funcionalidades)
- [Tecnologias](#-tecnologias)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Como as APIs funcionam](#-como-as-apis-funcionam)
- [Segurança](#-segurança)
- [Como rodar localmente](#-como-rodar-localmente)
- [Deploy em produção](#-deploy-em-produção)
- [Variáveis de ambiente](#-variáveis-de-ambiente)

---

## 🎯 Objetivo

O **Rancho Comanche** é um sistema interno para restaurantes que precisam gerenciar equipes de freelancers. O sistema permite:

- Cadastrar profissionais com endereço, especialidade e dados bancários
- Criar escalas de trabalho com cálculo automático de valor
- Controlar pagamentos via PIX com confirmação dupla
- Rastrear todas as ações do sistema em um log de auditoria
- Calcular automaticamente a distância dos freelancers até o restaurante (via CEP)

---

## ✅ Funcionalidades

| Módulo | Funcionalidades |
|---|---|
| 🔐 **Autenticação** | Login com JWT, logout, cadastro, recuperação de senha |
| 👥 **Freelancers** | CRUD completo com busca por nome/especialidade, status e distância |
| 📅 **Escala** | Criação e gestão de turnos com cálculo automático de horas e valor |
| 💰 **Pagamentos** | Listagem de escalas confirmadas, marcação de pago com confirmação dupla |
| 📍 **CEP / Distância** | Autopreenchimento de endereço via ViaCEP e cálculo de distância via Haversine / OpenRouteService |
| 🔑 **PIX** | Armazenamento de chave PIX criptografada com AES-256-GCM |
| 🛡️ **Auditoria** | Log completo de ações: login, logout, criação, edição, exclusão e visualização de dados sensíveis |

---

## 🛠️ Tecnologias

### Frontend (`web/`)
| Tecnologia | Uso |
|---|---|
| HTML5 / CSS3 | Estrutura e estilo das páginas |
| JavaScript ES2022 | Lógica de negócio, chamadas de API, routing |
| Web Crypto API | Criptografia AES-256-GCM nativa do browser |
| Font Awesome 6 | Ícones |
| **ViaCEP** (API externa) | Consulta de endereço por CEP — gratuita, sem chave |
| **Nominatim / OpenStreetMap** (API externa) | Geocodificação (CEP → coordenadas) — gratuita, sem chave |
| **OpenRouteService** (API externa) | Cálculo de distância por estradas — gratuita com cadastro |

### Backend (`backend/`)
| Tecnologia | Uso |
|---|---|
| Java 17 | Linguagem principal |
| Spring Boot 3.2 | Framework web, DI, configuração |
| Spring Security | Autenticação JWT, controle de acesso, CORS |
| Spring Data JPA + Hibernate | Persistência de dados |
| JJWT 0.11.5 | Geração e validação de tokens JWT (HMAC-SHA256) |
| H2 Database | Banco de dados (arquivo local em dev, in-memory em testes) |
| BCrypt | Hash de senhas |
| Spring Mail | Envio de e-mail para recuperação de senha |
| Maven | Build e gerenciamento de dependências |

---

## 📁 Estrutura do Projeto

```
Rancho-Poc/
│
├── web/                          ← Frontend (HTML/CSS/JS)
│   ├── Src/
│   │   ├── index.html            ← Página de login
│   │   ├── app.html              ← Painel principal (Dashboard, Freelancers, Escala, Pagamentos, Auditoria)
│   │   ├── cadastro.html         ← Cadastro de novo usuário
│   │   └── recuperar-senha.html  ← Fluxo de recuperação de senha
│   │
│   ├── css/
│   │   └── rancho.css            ← Estilos globais (variáveis, componentes, responsivo)
│   │
│   ├── js/
│   │   ├── config.js             ← URL do backend (auto-detecta dev/produção)
│   │   ├── auth.js               ← AuthService: login, cadastro, recuperação de senha
│   │   ├── api-service.js        ← FreelancerAPI, EscalaAPI, PagamentoAPI (com modo demo)
│   │   ├── app.js                ← Lógica principal: CRUD, dashboard, pagamentos, auditoria
│   │   ├── audit.js              ← AuditService: log de ações no localStorage
│   │   └── crypto.js             ← CryptoService: AES-256-GCM (Web Crypto API)
│   │
│   └── api/
│       ├── cep.js                ← ViaCEP + Nominatim (endereço e coordenadas por CEP)
│       ├── distancia.js          ← Haversine (linha reta) + OpenRouteService (estradas)
│       └── exemplo.js            ← Exemplos de uso das APIs externas
│
└── backend/                      ← Backend (Java Spring Boot)
    └── projeto-rancho/
        ├── pom.xml
        └── src/main/java/sptech/school/projeto_rancho/
            │
            ├── ProjetoRanchoApplication.java   ← Main
            ├── SecurityConfig.java             ← CORS + JWT + rotas públicas/protegidas
            ├── DataLoader.java                 ← Dados iniciais de exemplo
            │
            ├── controller/
            │   ├── AuthController.java         ← POST /api/auth/login | /cadastro | /recuperar-senha
            │   ├── FreelancerController.java   ← CRUD /api/freelancers
            │   ├── EscalaController.java       ← CRUD /api/escalas
            │   └── DistanciaController.java    ← POST /api/distancia/calcular
            │
            ├── model/
            │   ├── Usuario.java                ← Entidade de usuário (login)
            │   ├── Freelancer.java             ← Entidade de freelancer + PIX
            │   └── Escala.java                 ← Entidade de escala (turno de trabalho)
            │
            ├── dto/
            │   ├── FreelancerDTO.java
            │   ├── EscalaDTO.java
            │   └── auth/
            │       ├── LoginRequest.java
            │       ├── LoginResponse.java
            │       ├── CadastroRequest.java
            │       └── UserDTO.java
            │
            ├── repository/
            │   ├── FreelancerRepository.java   ← Queries customizadas com filtros
            │   ├── EscalaRepository.java       ← Queries por mês, freelancer, status
            │   └── UsuarioRepository.java
            │
            ├── Service/
            │   ├── AuthService.java            ← Login, cadastro, recuperação de senha
            │   ├── FreelancerService.java      ← CRUD + validação de CPF duplicado
            │   └── EscalaService.java          ← CRUD + cálculo automático de valor total
            │
            ├── mapper/
            │   ├── FreelancerMapper.java       ← Entity ↔ DTO
            │   └── EscalaMapper.java
            │
            ├── security/
            │   ├── JwtUtil.java                ← Geração e validação de tokens JWT
            │   └── JwtFilter.java              ← Filtro que intercepta Authorization: Bearer
            │
            └── exception/
                ├── GlobalExceptionHandler.java
                └── RecursoNaoEncontradoException.java
```

---

## 🔌 Como as APIs funcionam

### Autenticação — `/api/auth`
> Todas as rotas de `/api/auth` são **públicas** (sem JWT).

```
POST /api/auth/login              → { email, senha } → { token, user }
POST /api/auth/cadastro           → { nome, email, senha, ... } → { id, email }
POST /api/auth/recuperar-senha    → { email } → envia código por e-mail
POST /api/auth/verificar-codigo   → { email, codigo } → { success }
POST /api/auth/redefinir-senha    → { email, novaSenha } → { success }
```

### Freelancers — `/api/freelancers` 🔒
```
GET    /api/freelancers                  → lista com filtros (?status=ativo&especialidade=Garçom&search=nome)
GET    /api/freelancers/{id}             → freelancer por ID
POST   /api/freelancers                  → cria novo freelancer
PUT    /api/freelancers/{id}             → atualiza freelancer
DELETE /api/freelancers/{id}             → remove freelancer
GET    /api/freelancers/proximos         → lista ativos dentro do raio (?raioKm=15)
```

### Escalas — `/api/escalas` 🔒
```
GET    /api/escalas                      → lista com filtros (?freelancerId=1&status=confirmado)
GET    /api/escalas/{id}                 → escala por ID
POST   /api/escalas                      → cria nova escala (valorTotal calculado automaticamente)
PUT    /api/escalas/{id}                 → atualiza escala
DELETE /api/escalas/{id}                 → remove escala
GET    /api/escalas/mes/{ano}/{mes}       → escalas do mês (ex: /mes/2025/5)
GET    /api/escalas/resumo               → totais financeiros do mês
```

### APIs Externas (frontend)
```
ViaCEP           → https://viacep.com.br/ws/{cep}/json/       (endereço)
Nominatim        → https://nominatim.openstreetmap.org/search  (coordenadas)
OpenRouteService → https://api.openrouteservice.org/v2/...     (distância por estradas)
```

O frontend detecta o backend automaticamente:
- **localhost** → `http://localhost:8080/api` + modo demo ativado
- **produção** → URL configurada em `js/config.js` + modo demo desativado

O **modo demo** usa `localStorage` como banco de dados quando o backend está offline, permitindo testar o frontend sem precisar rodar o Spring Boot.

---

## 🔒 Segurança

| Camada | Implementação |
|---|---|
| **Senhas** | BCrypt (hash unidirecional) |
| **Autenticação** | JWT (HMAC-SHA256), expiração de 24h, stateless |
| **Rotas** | Spring Security — todas protegidas exceto `/api/auth/**` |
| **CORS** | Configurado para aceitar `localhost`, `*.github.io`, `*.onrender.com`, `*.railway.app` e URL customizada via env var |
| **Dados PIX** | AES-256-GCM via Web Crypto API nativa — criptografados no frontend antes de enviar ao backend |
| **Auditoria** | Log de todas as ações (login, CRUD, pagamentos, acesso a dados sensíveis) |

---

## 🚀 Como rodar localmente

### Pré-requisitos
- Java 17+
- Maven 3.8+
- Nenhuma instalação de banco necessária (usa H2 em arquivo)

### Backend
```bash
cd backend/projeto-rancho
./mvnw spring-boot:run
```
O backend sobe em `http://localhost:8080`.  
H2 Console disponível em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./rancho-db`).

### Frontend
Abra diretamente no browser:
```
web/Src/index.html
```
Ou sirva com qualquer servidor estático (Live Server, Python, etc.):
```bash
cd web
python -m http.server 5500
# acesse: http://localhost:5500/Src/index.html
```

> **Sem backend rodando?** Tudo bem — o modo demo usa localStorage automaticamente.

---

## ☁️ Deploy em produção

### Backend → Render.com
1. Acesse [render.com](https://render.com) → **New Web Service** → conecte o repositório
2. Configure:
   - **Root Directory:** `backend/projeto-rancho`
   - **Build Command:** `./mvnw clean package -DskipTests`
   - **Start Command:** `java -jar target/*.jar`
3. Adicione as [variáveis de ambiente](#-variáveis-de-ambiente)

### Frontend → GitHub Pages
1. Vá em **Settings → Pages** do repositório
2. Source: branch `main`, pasta `/web`
3. Atualize `PRODUCTION_API_URL` em `web/js/config.js` com a URL do Render

---

## ⚙️ Variáveis de ambiente

Configure no painel do Render (ou qualquer outro host):

| Variável | Descrição | Exemplo |
|---|---|---|
| `JWT_SECRET` | Chave secreta para assinar os tokens JWT | `MinhaSuperChave123!@#` |
| `APP_FRONTEND_URL` | URL exata do frontend em produção (para CORS) | `https://miguellima11.github.io/Rancho-Poc` |
| `DATABASE_URL` | URL do banco de dados (padrão: H2 local) | `jdbc:postgresql://host/rancho` |
| `DATABASE_USERNAME` | Usuário do banco | `postgres` |
| `DATABASE_PASSWORD` | Senha do banco | `senha123` |
| `DATABASE_DRIVER` | Driver JDBC | `org.postgresql.Driver` |
| `JPA_DIALECT` | Dialeto Hibernate | `org.hibernate.dialect.PostgreSQLDialect` |
| `MAIL_USERNAME` | E-mail para envio de recuperação de senha | `seuemail@gmail.com` |
| `MAIL_PASSWORD` | Senha de app do Gmail | `xxxx xxxx xxxx xxxx` |
| `H2_CONSOLE_ENABLED` | Habilitar console H2 (desabilite em produção) | `false` |

---

## 👥 Equipe

Desenvolvido como POC (Prova de Conceito) para o **Rancho Comanche** 🤠
