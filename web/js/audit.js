/**
 * RANCHO COMANCHE — Audit Service
 * Registra todas as ações importantes para rastreabilidade e segurança.
 * Armazenado no localStorage sob 'rc_audit_log' (máximo 500 entradas).
 *
 * Inclua este arquivo ANTES de app.js nos HTMLs.
 */

// Mapeamento de ações para rótulos, ícones e cor de badge na UI
const AUDIT_LABELS = {
  LOGIN:                    { label: 'Login realizado',           icon: '🔐', cor: 'badge-info'    },
  LOGOUT:                   { label: 'Logout',                    icon: '🚪', cor: 'badge-neutral'  },
  FREELANCER_CRIADO:        { label: 'Freelancer cadastrado',     icon: '👤', cor: 'badge-success'  },
  FREELANCER_EDITADO:       { label: 'Freelancer editado',        icon: '✏️',  cor: 'badge-warning'  },
  FREELANCER_EXCLUIDO:      { label: 'Freelancer excluído',       icon: '🗑️', cor: 'badge-error'    },
  ESCALA_CRIADA:            { label: 'Escala criada',             icon: '📅', cor: 'badge-success'  },
  ESCALA_EDITADA:           { label: 'Escala editada',            icon: '✏️',  cor: 'badge-warning'  },
  ESCALA_EXCLUIDA:          { label: 'Escala excluída',           icon: '🗑️', cor: 'badge-error'    },
  PAGAMENTO_REGISTRADO:     { label: 'Pagamento registrado',      icon: '💰', cor: 'badge-success'  },
  DADO_SENSIVEL_VISUALIZADO:{ label: 'Dado sensível visualizado', icon: '🔑', cor: 'badge-warning'  },
};

const AuditService = {

  /**
   * Registra uma ação.
   * @param {string} acao   - Chave da ação (ver AUDIT_LABELS)
   * @param {Object} detalhe - Dados contextuais da ação
   */
  log(acao, detalhe = {}) {
    let usuario   = 'sistema';
    let usuarioId = null;
    try {
      const u   = JSON.parse(localStorage.getItem('rc_user') || '{}');
      usuario   = u.email || u.name || 'sistema';
      usuarioId = u.id    || null;
    } catch (_) {}

    const entrada = {
      id:        Date.now() + Math.floor(Math.random() * 1000),
      timestamp: new Date().toISOString(),
      usuario,
      usuarioId,
      acao,
      detalhe,
    };

    const logs = this.getLogs();
    logs.unshift(entrada);                          // mais recente primeiro
    if (logs.length > 500) logs.splice(500);        // limite de 500 entradas
    try { localStorage.setItem('rc_audit_log', JSON.stringify(logs)); } catch (_) {}
    return entrada;
  },

  /** Retorna todos os logs armazenados. */
  getLogs(filtroAcao = '') {
    try {
      const all = JSON.parse(localStorage.getItem('rc_audit_log')) || [];
      return filtroAcao ? all.filter(l => l.acao === filtroAcao) : all;
    } catch { return []; }
  },

  /** Remove todos os logs (depois de confirmação). */
  limpar() {
    localStorage.removeItem('rc_audit_log');
    if (typeof renderAuditoria === 'function') renderAuditoria();
  },
};

window.AuditService = AuditService;
window.AUDIT_LABELS = AUDIT_LABELS;
