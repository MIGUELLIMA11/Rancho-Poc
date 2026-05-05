/**
 * RANCHO COMANCHE — CryptoService
 * Criptografia AES-256-GCM para dados sensíveis (chave PIX, dados bancários).
 * Usa a Web Crypto API nativa do browser — zero dependências externas.
 *
 * Algoritmo  : AES-GCM com chave de 256 bits
 * Derivação  : PBKDF2-SHA256, 100.000 iterações
 * IV         : 96 bits aleatórios por operação (padrão recomendado para AES-GCM)
 * Formato    : Base64(IV 12 bytes || Ciphertext)
 *
 * ⚠️ AVISO DE SEGURANÇA:
 *    Em produção, a chave deve ser gerenciada pelo backend (ex: recebida no login via HTTPS).
 *    Nunca exponha a chave derivada em logs, consoles ou respostas de API.
 */

const CryptoService = {
  _key: null,

  // Salt fixo da aplicação — pode ser externalizado como variável de ambiente no backend
  _APP_SALT: new TextEncoder().encode('rancho-comanche-aes256-v1-2024'),

  // ── Derivação de chave via PBKDF2 ─────────────────────────────────
  async _deriveKey(passphrase) {
    const keyMaterial = await window.crypto.subtle.importKey(
      'raw',
      new TextEncoder().encode(passphrase),
      'PBKDF2',
      false,
      ['deriveKey']
    );
    return window.crypto.subtle.deriveKey(
      {
        name:       'PBKDF2',
        salt:       this._APP_SALT,
        iterations: 100_000,   // OWASP recomenda ≥ 100.000 para SHA-256
        hash:       'SHA-256',
      },
      keyMaterial,
      { name: 'AES-GCM', length: 256 },
      false,                   // não exportável
      ['encrypt', 'decrypt']
    );
  },

  /**
   * Inicializa a chave derivada da identidade do usuário logado.
   * Chamado automaticamente antes de qualquer encrypt/decrypt.
   */
  async init() {
    if (this._key) return;
    try {
      const u    = JSON.parse(localStorage.getItem('rc_user') || '{}');
      const pass = (u.email || 'anonimo@rc') + '-rc-secret';
      this._key  = await this._deriveKey(pass);
    } catch (e) {
      console.error('[CryptoService] Erro ao inicializar chave:', e);
    }
  },

  /** Descarta a chave da memória. Chamar no logout. */
  reset() { this._key = null; },

  // ── Encrypt ──────────────────────────────────────────────────────
  async encrypt(plaintext) {
    if (!plaintext) return '';
    await this.init();
    if (!this._key) throw new Error('Chave de criptografia indisponível.');

    const iv         = window.crypto.getRandomValues(new Uint8Array(12)); // IV único por operação
    const ciphertext = await window.crypto.subtle.encrypt(
      { name: 'AES-GCM', iv },
      this._key,
      new TextEncoder().encode(plaintext)
    );

    // Serializa: IV (12 bytes) + ciphertext → Base64
    const combined = new Uint8Array(12 + ciphertext.byteLength);
    combined.set(iv, 0);
    combined.set(new Uint8Array(ciphertext), 12);
    return btoa(String.fromCharCode(...combined));
  },

  // ── Decrypt ──────────────────────────────────────────────────────
  async decrypt(encoded) {
    if (!encoded) return '';
    await this.init();
    if (!this._key) throw new Error('Chave de criptografia indisponível.');

    const combined   = Uint8Array.from(atob(encoded), c => c.charCodeAt(0));
    const iv         = combined.slice(0, 12);
    const ciphertext = combined.slice(12);

    const plain = await window.crypto.subtle.decrypt(
      { name: 'AES-GCM', iv },
      this._key,
      ciphertext
    );
    return new TextDecoder().decode(plain);
  },

  /**
   * Descriptografa e retorna valor mascarado para exibição segura.
   * Exibe apenas os 4 primeiros caracteres + asteriscos.
   * Nunca expõe o valor completo na interface.
   */
  async decryptMasked(encoded) {
    if (!encoded) return '—';
    try {
      const plain = await this.decrypt(encoded);
      if (!plain) return '—';
      return plain.slice(0, 4) + '*'.repeat(Math.max(0, plain.length - 4));
    } catch {
      return '*** erro de descriptografia ***';
    }
  },

  /** Descriptografa com silenciamento de erro (retorna '' em caso de falha). */
  async safeDecrypt(encoded) {
    if (!encoded) return '';
    try { return await this.decrypt(encoded); }
    catch { return ''; }
  },
};

window.CryptoService = CryptoService;
