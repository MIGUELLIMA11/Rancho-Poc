/**
 * RANCHO COMANCHE — Configuração Central de API
 *
 * Este arquivo detecta automaticamente o ambiente (dev/produção)
 * e configura a URL do backend corretamente.
 *
 * ─── PARA SUBIR EM PRODUÇÃO ──────────────────────────────────
 * Altere APENAS a linha PRODUCTION_API_URL abaixo com a URL
 * real do seu backend hospedado (Render, Railway, etc.).
 * ─────────────────────────────────────────────────────────────
 *
 * INCLUA ESTE ARQUIVO ANTES de api-service.js, auth.js e audit.js
 * nos HTMLs:
 *   <script src="../js/config.js"></script>
 */

// ── URL do backend em produção — ALTERE AQUI ─────────────────
// Exemplo Render:  'https://projeto-rancho.onrender.com/api'
// Exemplo Railway: 'https://projeto-rancho.up.railway.app/api'
const PRODUCTION_API_URL = 'https://SEU-BACKEND.onrender.com/api';

// ── Detecção automática de ambiente ──────────────────────────
const _isLocal = ['localhost', '127.0.0.1', ''].includes(window.location.hostname);

const RC_CONFIG = {
  // URL do Backend — localhost em dev, PRODUCTION_API_URL em produção
  API_URL: _isLocal ? 'http://localhost:8080/api' : PRODUCTION_API_URL,

  // Versão do App
  VERSION: '1.0.0',

  // Timeout de requests (ms)
  REQUEST_TIMEOUT: 10000,

  // Modo Demo (localStorage como fallback quando backend offline)
  // true  → ativo em localhost (facilita desenvolvimento sem backend)
  // false → desativado em produção (exige backend real)
  DEMO_MODE_ENABLED: _isLocal,
};

// Exporta para uso nos outros scripts
window.RC_CONFIG = RC_CONFIG;

console.info(
  `%c🤠 Rancho Comanche v${RC_CONFIG.VERSION}`,
  'color: #b5451b; font-weight: bold; font-size: 14px;'
);
console.info(
  `%c${_isLocal ? '🔧 DEV' : '🚀 PROD'} — Backend: ${RC_CONFIG.API_URL}`,
  `color: ${_isLocal ? '#b5451b' : '#2D7A4F'}; font-weight: 600;`
);
