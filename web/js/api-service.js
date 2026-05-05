/**
 * RANCHO COMANCHE — API Service
 * Centraliza todas as chamadas ao backend Spring Boot.
 * Inclui gestão de token JWT e tratamento de erros.
 *
 * URL do backend configurada em js/config.js → RC_CONFIG.API_URL
 */

const API_BASE = (window.RC_CONFIG && window.RC_CONFIG.API_URL)
  ? window.RC_CONFIG.API_URL
  : 'http://localhost:8080/api';

// ── Helpers ──────────────────────────────────────────────────

function getToken() {
  return localStorage.getItem('rc_token') || '';
}

// Headers para GET/DELETE (sem body)
function authHeaders() {
  return {
    'Authorization': 'Bearer ' + getToken()
  };
}

// Headers para POST/PUT (com body JSON)
function authJsonHeaders() {
  return {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + getToken()
  };
}

async function handleResponse(res) {
  if (res.status === 401) {
    localStorage.removeItem('rc_token');
    localStorage.removeItem('rc_user');
    window.location.href = 'index.html';
    throw new Error('Sessão expirada. Faça login novamente.');
  }

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message || `Erro ${res.status}`);
  }

  return res.json().catch(() => ({}));
}

// ── Modo Demo ────────────────────────────────────────────────
// Detecta se o backend está offline e usa localStorage como fallback
// Desative em produção: RC_CONFIG.DEMO_MODE_ENABLED = false

function isDemoMode(err) {
  const demoAtivo = window.RC_CONFIG ? window.RC_CONFIG.DEMO_MODE_ENABLED : true;
  return demoAtivo && err instanceof TypeError && err.message.includes('fetch');
}

// ── LocalStorage helpers (modo demo) ────────────────────────

function lsGet(key, def = []) {
  try { return JSON.parse(localStorage.getItem(key)) || def; }
  catch { return def; }
}

function lsSet(key, data) {
  localStorage.setItem(key, JSON.stringify(data));
}

function nextId(list) {
  return list.length ? Math.max(...list.map(i => i.id || 0)) + 1 : 1;
}

// ══════════════════════════════════════════════════════════════
// FREELANCERS API
// ══════════════════════════════════════════════════════════════

const FreelancerAPI = {
  /**
   * Listar todos os freelancers
   * GET /api/freelancers
   */
  async listar() {
    try {
      const res = await fetch(`${API_BASE}/freelancers`, { headers: authHeaders() });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) return lsGet('rc_freelancers');
      throw err;
    }
  },

  /**
   * Buscar por ID
   * GET /api/freelancers/{id}
   */
  async buscar(id) {
    try {
      const res = await fetch(`${API_BASE}/freelancers/${id}`, { headers: authHeaders() });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        const list = lsGet('rc_freelancers');
        return list.find(f => f.id === id) || null;
      }
      throw err;
    }
  },

  /**
   * Criar novo freelancer
   * POST /api/freelancers
   * Body: FreelancerDTO
   */
  async criar(dados) {
    try {
      const res = await fetch(`${API_BASE}/freelancers`, {
        method: 'POST',
        headers: authJsonHeaders(),
        body: JSON.stringify(dados)
      });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        const list = lsGet('rc_freelancers');
        const novo = { ...dados, id: nextId(list), criadoEm: new Date().toISOString() };
        list.push(novo);
        lsSet('rc_freelancers', list);
        return novo;
      }
      throw err;
    }
  },

  /**
   * Atualizar freelancer
   * PUT /api/freelancers/{id}
   */
  async atualizar(id, dados) {
    try {
      const res = await fetch(`${API_BASE}/freelancers/${id}`, {
        method: 'PUT',
        headers: authJsonHeaders(),
        body: JSON.stringify(dados)
      });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        let list = lsGet('rc_freelancers');
        list = list.map(f => f.id === id ? { ...f, ...dados } : f);
        lsSet('rc_freelancers', list);
        return list.find(f => f.id === id);
      }
      throw err;
    }
  },

  /**
   * Excluir freelancer
   * DELETE /api/freelancers/{id}
   */
  async excluir(id) {
    try {
      const res = await fetch(`${API_BASE}/freelancers/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
      });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        let list = lsGet('rc_freelancers');
        list = list.filter(f => f.id !== id);
        lsSet('rc_freelancers', list);
        return { success: true };
      }
      throw err;
    }
  }
};

// ══════════════════════════════════════════════════════════════
// ESCALA API
// ══════════════════════════════════════════════════════════════

const EscalaAPI = {
  /**
   * Listar todas as escalas
   * GET /api/escalas
   */
  async listar() {
    try {
      const res = await fetch(`${API_BASE}/escalas`, { headers: authHeaders() });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) return lsGet('rc_escalas');
      throw err;
    }
  },

  /**
   * Criar nova escala
   * POST /api/escalas
   */
  async criar(dados) {
    try {
      const res = await fetch(`${API_BASE}/escalas`, {
        method: 'POST',
        headers: authJsonHeaders(),
        body: JSON.stringify(dados)
      });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        const list = lsGet('rc_escalas');
        const nova = { ...dados, id: nextId(list), criadoEm: new Date().toISOString() };
        list.push(nova);
        lsSet('rc_escalas', list);
        return nova;
      }
      throw err;
    }
  },

  /**
   * Atualizar escala
   * PUT /api/escalas/{id}
   */
  async atualizar(id, dados) {
    try {
      const res = await fetch(`${API_BASE}/escalas/${id}`, {
        method: 'PUT',
        headers: authJsonHeaders(),
        body: JSON.stringify(dados)
      });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        let list = lsGet('rc_escalas');
        list = list.map(e => e.id === id ? { ...e, ...dados } : e);
        lsSet('rc_escalas', list);
        return list.find(e => e.id === id);
      }
      throw err;
    }
  },

  /**
   * Excluir escala
   * DELETE /api/escalas/{id}
   */
  async excluir(id) {
    try {
      const res = await fetch(`${API_BASE}/escalas/${id}`, {
        method: 'DELETE',
        headers: authHeaders()
      });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        let list = lsGet('rc_escalas');
        list = list.filter(e => e.id !== id);
        lsSet('rc_escalas', list);
        return { success: true };
      }
      throw err;
    }
  }
};

// ══════════════════════════════════════════════════════════════
// PAGAMENTOS API
// ══════════════════════════════════════════════════════════════

const PagamentoAPI = {
  /**
   * Listar pagamentos
   * GET /api/pagamentos
   */
  async listar() {
    try {
      const res = await fetch(`${API_BASE}/pagamentos`, { headers: authHeaders() });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) return lsGet('rc_pagamentos');
      throw err;
    }
  },

  /**
   * Marcar como pago
   * PUT /api/pagamentos/{id}/pagar
   */
  async marcarPago(id) {
    try {
      const res = await fetch(`${API_BASE}/pagamentos/${id}/pagar`, {
        method: 'PUT',
        headers: authHeaders()
      });
      return handleResponse(res);
    } catch (err) {
      if (isDemoMode(err)) {
        let list = lsGet('rc_pagamentos');
        list = list.map(p => p.id === id ? { ...p, statusPagamento: 'pago', dataPagamento: new Date().toISOString() } : p);
        lsSet('rc_pagamentos', list);
        return { success: true };
      }
      throw err;
    }
  }
};
