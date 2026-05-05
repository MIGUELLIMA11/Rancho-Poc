/**
 * RANCHO COMANCHE — AuthService
 * Conecta com o backend Spring Boot para autenticação.
 * URL configurada em js/config.js → RC_CONFIG.API_URL
 */

const AUTH_BASE_URL = (window.RC_CONFIG && window.RC_CONFIG.API_URL)
  ? window.RC_CONFIG.API_URL + '/auth'
  : 'http://localhost:8080/api/auth';

const AuthService = {
  /**
   * Realiza o login.
   * POST /api/auth/login
   * Body: { email, senha }
   * Returns: { token, user: { id, name, email, role } }
   */
  async login(email, senha) {
    try {
      const res = await fetch(`${AUTH_BASE_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, senha })
      });

      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Usuário ou senha incorretos.');
      }

      return await res.json();
    } catch (err) {
      // ── MODO DEMO (sem backend) ──────────────────────────────────
      // Remove este bloco quando o backend estiver no ar
      if (err instanceof TypeError && err.message.includes('fetch')) {
        console.warn('[AuthService] Backend não encontrado — usando modo demo.');
        if (email && senha && senha.length >= 4) {
          return {
            token: 'demo-token-' + Date.now(),
            user: {
              id: 1,
              name: email.split('@')[0].replace(/\./g, ' '),
              email: email,
              role: 'Gestor'
            }
          };
        }
        throw new Error('Usuário ou senha incorretos.');
      }
      throw err;
    }
  },

  /**
   * Cadastra novo usuário.
   * POST /api/auth/cadastro
   * Body: { nome, email, cpf, telefone, senha, ... (endereço) }
   */
  async register(dados) {
    try {
      const res = await fetch(`${AUTH_BASE_URL}/cadastro`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
      });

      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Erro ao criar conta.');
      }

      return await res.json();
    } catch (err) {
      // Modo demo
      if (err instanceof TypeError && err.message.includes('fetch')) {
        console.warn('[AuthService] Backend não encontrado — usando modo demo.');
        return { success: true, message: 'Conta criada (demo).' };
      }
      throw err;
    }
  },

  /**
   * Envia e-mail de recuperação de senha.
   * POST /api/auth/recuperar-senha
   * Body: { email }
   */
  async sendRecoveryEmail(email) {
    try {
      const res = await fetch(`${AUTH_BASE_URL}/recuperar-senha`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      });

      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'E-mail não encontrado no sistema.');
      }

      return await res.json();
    } catch (err) {
      // Modo demo
      if (err instanceof TypeError && err.message.includes('fetch')) {
        console.warn('[AuthService] Modo demo — código enviado simulado.');
        return { success: true };
      }
      throw err;
    }
  },

  /**
   * Verifica código de recuperação.
   * POST /api/auth/verificar-codigo
   * Body: { email, codigo }
   */
  async verifyCode(email, codigo) {
    try {
      const res = await fetch(`${AUTH_BASE_URL}/verificar-codigo`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, codigo })
      });

      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Código inválido ou expirado.');
      }

      return await res.json();
    } catch (err) {
      // Modo demo: aceita qualquer código de 6 dígitos
      if (err instanceof TypeError && err.message.includes('fetch')) {
        if (/^\d{6}$/.test(codigo)) return { success: true };
        throw new Error('Código inválido. Use 6 dígitos.');
      }
      throw err;
    }
  },

  /**
   * Redefine a senha do usuário.
   * POST /api/auth/redefinir-senha
   * Body: { email, novaSenha }
   */
  async resetPassword(email, novaSenha) {
    try {
      const res = await fetch(`${AUTH_BASE_URL}/redefinir-senha`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, novaSenha })
      });

      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Erro ao redefinir a senha.');
      }

      return await res.json();
    } catch (err) {
      // Modo demo
      if (err instanceof TypeError && err.message.includes('fetch')) {
        return { success: true };
      }
      throw err;
    }
  }
};
