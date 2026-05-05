/**
 * RANCHO COMANCHE — App Principal
 * Lógica de CRUD de Freelancers, Escala e Dashboard.
 * Integra com CEP API (api/cep.js) e API Service (js/api-service.js).
 */

// ══════════════════════════════════════════════════════════════
// CONFIGURAÇÃO DO RESTAURANTE
// ══════════════════════════════════════════════════════════════

const RANCHO = {
  nome:      'Rancho Comanche',
  cep:       '09834-203',
  latitude:  -23.513870,   // fallback — atualizado automaticamente abaixo
  longitude: -46.861780
};

// Atualiza coordenadas do Rancho pelo CEP real na inicialização
async function inicializarCoordsRancho() {
  try {
    const coords = await buscarCoordenadasPorEndereco(null, null, null, RANCHO.cep.replace('-', ''));
    RANCHO.latitude  = coords.latitude;
    RANCHO.longitude = coords.longitude;
    console.info(`📍 Rancho coords: ${coords.latitude}, ${coords.longitude}`);
  } catch (e) {
    console.warn('Usando coordenadas fixas do Rancho (fallback):', e.message);
  }
}

// ══════════════════════════════════════════════════════════════
// INICIALIZAÇÃO
// ══════════════════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', async () => {
  // Verifica autenticação
  const token = localStorage.getItem('rc_token');
  if (!token) {
    window.location.href = 'index.html';
    return;
  }

  // Atualiza coords do restaurante pelo CEP
  await inicializarCoordsRancho();

  // Carrega dados do usuário
  loadUserInfo();

  // Inicia filtro de mês atual na escala
  const mesAtual = new Date().toISOString().slice(0, 7);
  document.getElementById('esc-filter-mes').value = mesAtual;

  // Atualiza data do dashboard
  document.getElementById('dash-date').textContent =
    new Date().toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });

  // Carrega dados
  await Promise.all([renderFreelancers(), renderEscalas()]);
  updateDashboard();
  renderPagamentos();
});

function loadUserInfo() {
  const raw  = localStorage.getItem('rc_user');
  const user = raw ? JSON.parse(raw) : { name: 'Usuário', role: 'Gestor' };

  document.getElementById('user-name-label').textContent = user.name || user.email || 'Usuário';
  document.getElementById('user-role-label').textContent = user.role || 'Gestor';

  const initials = (user.name || 'U').split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
  document.getElementById('user-avatar-initials').textContent = initials;
}

// ══════════════════════════════════════════════════════════════
// NAVEGAÇÃO
// ══════════════════════════════════════════════════════════════

const VIEW_CONFIG = {
  dashboard:    { title: 'Dashboard',           subtitle: 'Visão geral do sistema' },
  freelancers:  { title: 'Gestão de Freelancers', subtitle: 'Profissionais cadastrados' },
  escala:       { title: 'Gestão de Escala',    subtitle: 'Organização dos turnos' },
  pagamentos:   { title: 'Pagamentos',          subtitle: 'Controle financeiro' },
  auditoria:    { title: 'Log de Auditoria',    subtitle: 'Rastreamento de ações e segurança' },
};

function showView(name) {
  // Esconde todas as views
  document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));

  // Ativa a view correta
  document.getElementById('view-' + name).classList.add('active');
  document.getElementById('nav-' + name).classList.add('active');

  const cfg = VIEW_CONFIG[name] || {};
  document.getElementById('page-title').textContent    = cfg.title    || name;
  document.getElementById('page-subtitle').textContent = cfg.subtitle || '';

  if (name === 'auditoria') renderAuditoria();

  closeSidebar();
}

// Sidebar mobile
function openSidebar() {
  document.getElementById('sidebar').classList.add('open');
  document.getElementById('sidebar-backdrop').classList.add('visible');
}

function closeSidebar() {
  document.getElementById('sidebar').classList.remove('open');
  document.getElementById('sidebar-backdrop').classList.remove('visible');
}

function logout() {
  AuditService.log('LOGOUT');
  CryptoService.reset();
  localStorage.removeItem('rc_token');
  localStorage.removeItem('rc_user');
  window.location.href = 'index.html';
}

// ══════════════════════════════════════════════════════════════
// MODAL HELPERS
// ══════════════════════════════════════════════════════════════

function abrirModal(id) {
  document.getElementById(id).classList.add('open');
  document.body.style.overflow = 'hidden';
}

function fecharModal(id) {
  document.getElementById(id).classList.remove('open');
  document.body.style.overflow = '';
}

// Fechar modal ao clicar no overlay
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal-overlay')) {
    e.target.classList.remove('open');
    document.body.style.overflow = '';
  }
});

// Fechar com ESC
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-overlay.open').forEach(m => {
      m.classList.remove('open');
    });
    document.body.style.overflow = '';
  }
});

// ══════════════════════════════════════════════════════════════
// TOAST NOTIFICATIONS
// ══════════════════════════════════════════════════════════════

function showToast(msg, type = 'success', duration = 3500) {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = 'toast toast-' + type;

  const icons = { success: 'fa-check-circle', error: 'fa-times-circle', warning: 'fa-exclamation-triangle' };
  toast.innerHTML = '<i class="fa ' + (icons[type] || 'fa-info-circle') + '"></i> ' + msg;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = '0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

// ══════════════════════════════════════════════════════════════
// MODAL ALERT HELPER
// ══════════════════════════════════════════════════════════════

function showModalAlert(alertId, msg, type = 'error') {
  const el = document.getElementById(alertId);
  el.className = 'alert alert-' + type;
  const icon = type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle';
  el.innerHTML = '<i class="fa ' + icon + '"></i> ' + msg;
}

function clearModalAlert(alertId) {
  const el = document.getElementById(alertId);
  el.className = 'alert hidden';
  el.textContent = '';
}

function setFieldError(id, on) {
  const el = document.getElementById(id);
  if (!el) return;
  el.closest('.form-group').classList.toggle('has-error', on);
}

// ══════════════════════════════════════════════════════════════
// DASHBOARD
// ══════════════════════════════════════════════════════════════

async function updateDashboard() {
  const freelancers = await FreelancerAPI.listar();
  const escalas     = await EscalaAPI.listar();

  const ativos      = freelancers.filter(f => f.status === 'ativo').length;
  const proximos    = freelancers.filter(f => (f.distanciaKm || 999) <= 15).length;

  const mesAtual    = new Date().toISOString().slice(0, 7);
  const escalasMes  = escalas.filter(e => (e.data || '').startsWith(mesAtual));
  const gastoMes    = escalasMes.reduce((sum, e) => sum + (parseFloat(e.valorTotal) || 0), 0);

  // Stats globais
  document.getElementById('stat-total-freelancers').textContent = freelancers.length;
  document.getElementById('stat-ativos').textContent = ativos;
  document.getElementById('stat-escalas-mes').textContent = escalasMes.length;
  document.getElementById('stat-gasto-mes').textContent = 'R$ ' + gastoMes.toFixed(2).replace('.', ',');

  // Badges sidebar
  document.getElementById('badge-freelancers').textContent = freelancers.length;
  // Conta escalas futuras (de hoje em diante) que não foram canceladas
  const hojeBadge = new Date().toISOString().slice(0, 10);
  document.getElementById('badge-escala').textContent = escalas.filter(e =>
    e.status !== 'cancelado' && (e.data || '') >= hojeBadge
  ).length;

  // Próximas escalas (próximas 5)
  const hoje = new Date().toISOString().slice(0, 10);
  const proxEscalas = escalas
    .filter(e => e.data >= hoje && e.status !== 'cancelado')
    .sort((a, b) => a.data.localeCompare(b.data))
    .slice(0, 4);

  const proxEl = document.getElementById('dash-proximas-escalas');
  if (proxEscalas.length === 0) {
    proxEl.innerHTML = '<div class="empty-state" style="padding:30px 20px;"><div class="empty-icon">📅</div><p>Nenhuma escala agendada</p></div>';
  } else {
    proxEl.innerHTML = proxEscalas.map(e => {
      const fl = freelancers.find(f => f.id === e.freelancerId) || {};
      return `
        <div class="schedule-card ${e.status}" style="margin-bottom:8px;">
          <div class="sc-time">
            <div class="time-range">${e.horaInicio || '--'}</div>
            <div class="time-hours">${formatDate(e.data)}</div>
          </div>
          <div class="sc-info">
            <div class="sc-name">${fl.nome || '—'}</div>
            <div class="sc-meta">${e.funcao || fl.especialidade || '—'}</div>
          </div>
          <span class="badge ${statusBadge(e.status)}">${e.status}</span>
        </div>`;
    }).join('');
  }

  // Freelancers recentes
  const recentes = [...freelancers].reverse().slice(0, 4);
  const recEl = document.getElementById('dash-freelancers-recentes');
  if (recentes.length === 0) {
    recEl.innerHTML = '<div class="empty-state" style="padding:30px 20px;"><div class="empty-icon">👥</div><p>Nenhum freelancer</p></div>';
  } else {
    recEl.innerHTML = `
      <table class="data-table">
        <tbody>
          ${recentes.map(f => `
            <tr>
              <td><div class="person-cell">
                <div class="avatar ${avatarColor(f.id)}">${initials(f.nome)}</div>
                <div><div class="person-name">${f.nome}</div><div class="person-sub">${f.especialidade || '—'}</div></div>
              </div></td>
              <td><span class="badge ${f.status === 'ativo' ? 'badge-success' : 'badge-neutral'}">${f.status}</span></td>
            </tr>`).join('')}
        </tbody>
      </table>`;
  }
}

// ══════════════════════════════════════════════════════════════
// FREELANCERS CRUD
// ══════════════════════════════════════════════════════════════

async function renderFreelancers() {
  const all    = await FreelancerAPI.listar();
  const search = (document.getElementById('fl-search')?.value || '').toLowerCase();
  const status = document.getElementById('fl-filter-status')?.value || '';
  const esp    = document.getElementById('fl-filter-esp')?.value    || '';

  const filtered = all.filter(f => {
    const matchSearch = !search ||
      (f.nome       || '').toLowerCase().includes(search) ||
      (f.email      || '').toLowerCase().includes(search) ||
      (f.especialidade || '').toLowerCase().includes(search);
    const matchStatus = !status || f.status === status;
    const matchEsp    = !esp    || f.especialidade === esp;
    return matchSearch && matchStatus && matchEsp;
  });

  // Stats
  document.getElementById('fl-total').textContent   = all.length;
  document.getElementById('fl-ativos').textContent  = all.filter(f => f.status === 'ativo').length;
  document.getElementById('fl-proximos').textContent = all.filter(f => (f.distanciaKm || 999) <= 15).length;

  const tbody = document.getElementById('fl-tbody');
  document.getElementById('fl-count-label').textContent = `${filtered.length} freelancer${filtered.length !== 1 ? 's' : ''}`;

  if (filtered.length === 0) {
    tbody.innerHTML = `
      <tr><td colspan="7">
        <div class="empty-state">
          <div class="empty-icon">👥</div>
          <h4>${search || status || esp ? 'Nenhum resultado encontrado' : 'Nenhum freelancer cadastrado'}</h4>
          <p>${search || status || esp ? 'Tente ajustar os filtros.' : 'Clique em "Novo Freelancer" para começar.'}</p>
        </div>
      </td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(f => {
    const distText = f.distanciaKm != null
      ? `${f.distanciaKm} km`
      : '<span class="text-muted text-sm">—</span>';
    const distClass = f.distanciaKm == null ? '' :
      f.distanciaKm <= 5  ? 'text-success' :
      f.distanciaKm <= 15 ? '' : 'text-error';

    return `
      <tr>
        <td>
          <div class="person-cell">
            <div class="avatar ${avatarColor(f.id)}">${initials(f.nome)}</div>
            <div>
              <div class="person-name">${esc(f.nome)}</div>
              <div class="person-sub">${esc(f.email || '—')}</div>
            </div>
          </div>
        </td>
        <td>${esc(f.especialidade || '—')}</td>
        <td>${esc(f.telefone || '—')}</td>
        <td>
          <div style="font-size:0.82rem; color:var(--text-mid);">${esc(f.cidade ? f.cidade + ' - ' + f.estado : '—')}</div>
          <div class="${distClass} fw-semi text-sm">${distText}</div>
        </td>
        <td class="fw-semi">${f.valorHora ? 'R$ ' + parseFloat(f.valorHora).toFixed(2).replace('.', ',') : '—'}</td>
        <td><span class="badge ${f.status === 'ativo' ? 'badge-success' : 'badge-neutral'}">${f.status || 'ativo'}</span></td>
        <td>
          <div class="table-actions">
            <button class="btn btn-sm btn-ghost" onclick="editarFreelancer(${f.id})" title="Editar">
              <i class="fa fa-edit"></i>
            </button>
            <button class="btn btn-sm btn-ghost" style="color:var(--error);" onclick="confirmarExclusaoFreelancer(${f.id})" title="Excluir">
              <i class="fa fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>`;
  }).join('');
}

// ── Abrir modal de novo freelancer ──
function abrirModalFreelancer() {
  limparFormFreelancer();
  document.getElementById('modal-fl-title').textContent    = 'Novo Freelancer';
  document.getElementById('modal-fl-subtitle').textContent = 'Preencha os dados do profissional';
  document.getElementById('btn-fl-save-text').textContent  = 'Salvar';
  abrirModal('modal-freelancer');
}

// ── Editar freelancer ──
async function editarFreelancer(id) {
  limparFormFreelancer();
  const f = (await FreelancerAPI.listar()).find(f => f.id === id);
  if (!f) { showToast('Freelancer não encontrado.', 'error'); return; }

  document.getElementById('fl-id').value             = f.id;
  document.getElementById('fl-nome').value           = f.nome           || '';
  document.getElementById('fl-cpf').value            = f.cpf            || '';
  document.getElementById('fl-telefone').value       = f.telefone       || '';
  document.getElementById('fl-email').value          = f.email          || '';
  document.getElementById('fl-especialidade').value  = f.especialidade  || '';
  document.getElementById('fl-valor-hora').value     = f.valorHora      || '';
  document.getElementById('fl-status').value         = f.status         || 'ativo';
  document.getElementById('fl-observacoes').value    = f.observacoes    || '';
  document.getElementById('fl-cep').value            = f.cep            || '';
  document.getElementById('fl-logradouro').value     = f.logradouro     || '';
  document.getElementById('fl-numero').value         = f.numero         || '';
  document.getElementById('fl-complemento').value    = f.complemento    || '';
  document.getElementById('fl-bairro').value         = f.bairro         || '';
  document.getElementById('fl-uf').value             = f.estado         || '';
  document.getElementById('fl-cidade').value         = f.cidade         || '';
  document.getElementById('fl-latitude').value       = f.latitude       || '';
  document.getElementById('fl-longitude').value      = f.longitude      || '';
  document.getElementById('fl-distancia-km').value   = f.distanciaKm    || '';

  // Descriptografa e preenche dados PIX
  document.getElementById('fl-pix-tipo').value = f.pixTipo || '';
  document.getElementById('fl-banco').value    = f.banco   || '';
  if (f.pixChave) {
    AuditService.log('DADO_SENSIVEL_VISUALIZADO', { freelancerId: f.id, nome: f.nome, campo: 'chavePix' });
    document.getElementById('fl-pix-chave').value = await CryptoService.safeDecrypt(f.pixChave);
  }

  if (f.cep) {
    const preview = document.getElementById('fl-address-preview');
    const cidade  = f.cidade ? `${f.cidade} - ${f.estado}` : '';
    document.getElementById('fl-addr-full').textContent = f.logradouro ? `${f.logradouro}, ${f.numero || 'S/N'} — ${cidade}` : cidade;
    if (f.distanciaKm) mostrarDistanciaPreview(f.distanciaKm);
    preview.classList.add('visible');
  }

  document.getElementById('modal-fl-title').textContent    = 'Editar Freelancer';
  document.getElementById('modal-fl-subtitle').textContent = 'Atualize os dados do profissional';
  document.getElementById('btn-fl-save-text').textContent  = 'Atualizar';
  abrirModal('modal-freelancer');
}

// ── Salvar freelancer ──
async function salvarFreelancer() {
  clearModalAlert('modal-fl-alert');

  const nome = document.getElementById('fl-nome').value.trim();
  const tel  = document.getElementById('fl-telefone').value.trim();
  const esp  = document.getElementById('fl-especialidade').value;
  const vHora = document.getElementById('fl-valor-hora').value;
  const cep   = document.getElementById('fl-cep').value.trim();

  let valid = true;
  if (!nome)  { setFieldError('fl-nome', true);          valid = false; } else setFieldError('fl-nome', false);
  if (!tel)   { setFieldError('fl-telefone', true);      valid = false; } else setFieldError('fl-telefone', false);
  if (!esp)   { setFieldError('fl-especialidade', true); valid = false; } else setFieldError('fl-especialidade', false);
  if (!vHora) { setFieldError('fl-valor-hora', true);    valid = false; } else setFieldError('fl-valor-hora', false);
  if (!cep || cep.replace(/\D/g,'').length !== 8) { setFieldError('fl-cep', true); valid = false; } else setFieldError('fl-cep', false);

  if (!valid) { showModalAlert('modal-fl-alert', 'Preencha todos os campos obrigatórios.'); return; }

  const dados = {
    nome,
    cpf:          document.getElementById('fl-cpf').value.trim(),
    telefone:     tel,
    email:        document.getElementById('fl-email').value.trim(),
    especialidade: esp,
    valorHora:    parseFloat(vHora),
    status:       document.getElementById('fl-status').value,
    observacoes:  document.getElementById('fl-observacoes').value.trim(),
    cep:          document.getElementById('fl-cep').value.trim(),
    logradouro:   document.getElementById('fl-logradouro').value,
    numero:       document.getElementById('fl-numero').value.trim(),
    complemento:  document.getElementById('fl-complemento').value.trim(),
    bairro:       document.getElementById('fl-bairro').value,
    cidade:       document.getElementById('fl-cidade').value,
    estado:       document.getElementById('fl-uf').value,
    latitude:     parseFloat(document.getElementById('fl-latitude').value) || null,
    longitude:    parseFloat(document.getElementById('fl-longitude').value) || null,
    distanciaKm:  parseFloat(document.getElementById('fl-distancia-km').value) || null,
  };

  setBtnLoading('btn-salvar-freelancer', 'btn-fl-save-text', 'btn-fl-spinner', true);

  // Criptografa chave PIX antes de salvar
  const pixChaveRaw = document.getElementById('fl-pix-chave').value.trim();
  if (pixChaveRaw) {
    try { dados.pixChave = await CryptoService.encrypt(pixChaveRaw); } catch (_) {}
  }
  dados.pixTipo = document.getElementById('fl-pix-tipo').value;
  dados.banco   = document.getElementById('fl-banco').value.trim();

  try {
    const id = document.getElementById('fl-id').value;
    if (id) {
      const updated = await FreelancerAPI.atualizar(parseInt(id), dados);
      AuditService.log('FREELANCER_EDITADO', { id: parseInt(id), nome: dados.nome });
      showToast('Freelancer atualizado com sucesso!', 'success');
    } else {
      const created = await FreelancerAPI.criar(dados);
      AuditService.log('FREELANCER_CRIADO', { id: created?.id, nome: dados.nome, especialidade: dados.especialidade });
      showToast('Freelancer cadastrado com sucesso!', 'success');
    }

    fecharModal('modal-freelancer');
    await renderFreelancers();
    updateDashboard();
  } catch (err) {
    showModalAlert('modal-fl-alert', err.message || 'Erro ao salvar.');
  } finally {
    setBtnLoading('btn-salvar-freelancer', 'btn-fl-save-text', 'btn-fl-spinner', false);
  }
}

// ── Confirmar exclusão ──
function confirmarExclusaoFreelancer(id) {
  document.getElementById('confirm-title').textContent = 'Excluir freelancer?';
  document.getElementById('confirm-msg').textContent   = 'Todas as escalas associadas serão afetadas. Esta ação não pode ser desfeita.';

  const btn = document.getElementById('btn-confirmar-delete');
  btn.onclick = async () => {
    fecharModal('modal-confirmar');
    try {
      // Busca nome antes de excluir para o log
      const fl = (await FreelancerAPI.listar()).find(f => f.id === id) || {};
      await FreelancerAPI.excluir(id);
      AuditService.log('FREELANCER_EXCLUIDO', { id, nome: fl.nome || '—' });
      showToast('Freelancer excluído.', 'success');
      await renderFreelancers();
      updateDashboard();
    } catch (err) {
      showToast(err.message || 'Erro ao excluir.', 'error');
    }
  };

  abrirModal('modal-confirmar');
}

// ── CEP autocomplete no modal de freelancer ──
document.getElementById('fl-cep').addEventListener('input', function () {
  let v = this.value.replace(/\D/g, '').slice(0, 8);
  if (v.length > 5) v = v.slice(0, 5) + '-' + v.slice(5);
  this.value = v;
  if (v.replace('-', '').length === 8) buscarCepFreelancer();
});

async function buscarCepFreelancer() {
  const cepVal = document.getElementById('fl-cep').value.replace(/\D/g, '');
  if (cepVal.length !== 8) return;

  const statusEl = document.getElementById('fl-cep-status');
  statusEl.className = 'cep-status loading';
  statusEl.innerHTML = '<span class="cep-spinner"></span> Buscando...';

  try {
    const dados = await buscarEnderecoPorCep(cepVal);

    document.getElementById('fl-logradouro').value = dados.logradouro || '';
    document.getElementById('fl-bairro').value     = dados.bairro     || '';
    document.getElementById('fl-cidade').value     = dados.cidade     || '';
    document.getElementById('fl-uf').value         = dados.estado     || '';

    statusEl.className = 'cep-status success';
    statusEl.innerHTML = '<i class="fa fa-check-circle"></i> Endereço encontrado';

    // Busca coordenadas e calcula distância
    try {
      const coords = await buscarCoordenadasPorEndereco(
        dados.enderecoCompleto,
        dados.cidade,
        dados.estado,
        cepVal
      );
      document.getElementById('fl-latitude').value  = coords.latitude;
      document.getElementById('fl-longitude').value = coords.longitude;

      const dist = calcularDistanciaHaversine(
        RANCHO.latitude, RANCHO.longitude,
        coords.latitude, coords.longitude
      );
      document.getElementById('fl-distancia-km').value = dist;
      mostrarDistanciaPreview(dist);
    } catch (e) {
      console.warn('Coordenadas não encontradas:', e.message);
    }

    // Preview
    const preview = document.getElementById('fl-address-preview');
    document.getElementById('fl-addr-full').textContent = dados.enderecoCompleto;
    preview.classList.add('visible');

    setFieldError('fl-cep', false);
    document.getElementById('fl-numero').focus();
  } catch (err) {
    statusEl.className = 'cep-status error';
    statusEl.innerHTML = '<i class="fa fa-times-circle"></i> ' + err.message;
    document.getElementById('fl-address-preview').classList.remove('visible');
  }
}

function mostrarDistanciaPreview(dist) {
  const chip = document.getElementById('fl-dist-badge');
  const row  = document.getElementById('fl-dist-row');
  let cls = '', label = '';

  if (dist <= 5)       { cls = 'dist-near';   label = '🟢 ' + dist + ' km — Muito próximo'; }
  else if (dist <= 15) { cls = 'dist-medium'; label = '🟡 ' + dist + ' km — Distância média'; }
  else                 { cls = 'dist-far';    label = '🔴 ' + dist + ' km — Distante'; }

  chip.className = 'distance-chip ' + cls;
  chip.textContent = label;
  row.style.display = 'flex';
}

// ── Limpar form freelancer ──
function limparFormFreelancer() {
  document.getElementById('form-freelancer').reset();
  document.getElementById('fl-id').value = '';
  document.getElementById('fl-address-preview').classList.remove('visible');
  document.getElementById('fl-cep-status').innerHTML = '';
  document.getElementById('fl-dist-badge').textContent = '';
  document.getElementById('fl-dist-row').style.display = 'none';
  document.getElementById('fl-pix-chave').value = '';
  document.getElementById('fl-pix-tipo').value  = '';
  document.getElementById('fl-banco').value     = '';
  clearModalAlert('modal-fl-alert');
  document.querySelectorAll('#form-freelancer .form-group').forEach(g => g.classList.remove('has-error'));
}

// ══════════════════════════════════════════════════════════════
// ESCALA CRUD
// ══════════════════════════════════════════════════════════════

async function renderEscalas() {
  const all        = await EscalaAPI.listar();
  const freelancers = await FreelancerAPI.listar();
  const search     = (document.getElementById('esc-search')?.value || '').toLowerCase();
  const filtroMes  = document.getElementById('esc-filter-mes')?.value || '';
  const filtroSt   = document.getElementById('esc-filter-status')?.value || '';

  const filtered = all.filter(e => {
    const fl = freelancers.find(f => f.id === e.freelancerId) || {};
    const matchSearch = !search ||
      (fl.nome           || '').toLowerCase().includes(search) ||
      (e.funcao          || '').toLowerCase().includes(search);
    const matchMes = !filtroMes || (e.data || '').startsWith(filtroMes);
    const matchSt  = !filtroSt  || e.status === filtroSt;
    return matchSearch && matchMes && matchSt;
  });

  // Ordena por data
  filtered.sort((a, b) => a.data.localeCompare(b.data));

  // Stats
  document.getElementById('esc-total').textContent       = all.length;
  document.getElementById('esc-confirmadas').textContent = all.filter(e => e.status === 'confirmado').length;
  document.getElementById('esc-pendentes').textContent   = all.filter(e => e.status === 'pendente').length;
  const gasto = all.reduce((s, e) => s + (parseFloat(e.valorTotal) || 0), 0);
  document.getElementById('esc-gasto').textContent = 'R$ ' + gasto.toFixed(2).replace('.', ',');

  const el = document.getElementById('esc-list');

  if (filtered.length === 0) {
    el.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">📅</div>
        <h4>${search || filtroMes || filtroSt ? 'Nenhum resultado' : 'Nenhuma escala cadastrada'}</h4>
        <p>${search || filtroMes || filtroSt ? 'Ajuste os filtros.' : 'Clique em "Nova Escala" para começar.'}</p>
      </div>`;
    return;
  }

  // Agrupa por data
  const grupos = {};
  filtered.forEach(e => {
    if (!grupos[e.data]) grupos[e.data] = [];
    grupos[e.data].push(e);
  });

  el.innerHTML = Object.entries(grupos).map(([data, lista]) => `
    <div class="schedule-date-group">
      <div class="schedule-date-label">
        <i class="fa fa-calendar-day"></i> ${formatDate(data)} — ${lista.length} escala${lista.length !== 1 ? 's' : ''}
      </div>
      ${lista.map(e => {
        const fl = freelancers.find(f => f.id === e.freelancerId) || {};
        const dur = calcularDuracao(e.horaInicio, e.horaFim);
        return `
          <div class="schedule-card ${e.status}">
            <div class="sc-time">
              <div class="time-range">${e.horaInicio || '--'}–${e.horaFim || '--'}</div>
              <div class="time-hours">${dur ? dur + 'h' : '—'}</div>
            </div>
            <div class="sc-info">
              <div class="sc-name">${esc(fl.nome || 'Freelancer não encontrado')}</div>
              <div class="sc-meta">${esc(e.funcao || fl.especialidade || '—')}${e.observacoes ? ' · ' + esc(e.observacoes) : ''}</div>
            </div>
            <span class="badge ${statusBadge(e.status)}">${e.status}</span>
            <div class="sc-value">${e.valorTotal ? 'R$ ' + parseFloat(e.valorTotal).toFixed(2).replace('.', ',') : '—'}</div>
            <div class="sc-actions">
              <button class="btn btn-sm btn-ghost" onclick="editarEscala(${e.id})" title="Editar"><i class="fa fa-edit"></i></button>
              <button class="btn btn-sm btn-ghost" style="color:var(--error);" onclick="confirmarExclusaoEscala(${e.id})" title="Excluir"><i class="fa fa-trash"></i></button>
            </div>
          </div>`;
      }).join('')}
    </div>`
  ).join('');
}

// ── Abrir modal de nova escala ──
async function abrirModalEscala() {
  limparFormEscala();

  // Popula select de freelancers ativos
  await populateFreelancerSelect();

  // Data padrão: hoje
  document.getElementById('esc-data').value = new Date().toISOString().slice(0, 10);

  document.getElementById('modal-esc-title').textContent    = 'Nova Escala';
  document.getElementById('modal-esc-subtitle').textContent = 'Defina data, horário e profissional';
  document.getElementById('btn-esc-save-text').textContent  = 'Salvar';
  abrirModal('modal-escala');
}

// ── Editar escala ──
async function editarEscala(id) {
  limparFormEscala();
  await populateFreelancerSelect();

  const list = await EscalaAPI.listar();
  const e = list.find(e => e.id === id);
  if (!e) { showToast('Escala não encontrada.', 'error'); return; }

  document.getElementById('esc-id').value          = e.id;
  document.getElementById('esc-data').value        = e.data        || '';
  document.getElementById('esc-freelancer').value  = e.freelancerId || '';
  document.getElementById('esc-hora-inicio').value = e.horaInicio  || '';
  document.getElementById('esc-hora-fim').value    = e.horaFim     || '';
  document.getElementById('esc-funcao').value      = e.funcao      || '';
  document.getElementById('esc-status').value      = e.status      || 'pendente';
  document.getElementById('esc-observacoes').value = e.observacoes || '';

  calcularValorEscala();

  document.getElementById('modal-esc-title').textContent    = 'Editar Escala';
  document.getElementById('modal-esc-subtitle').textContent = 'Atualize os dados da escala';
  document.getElementById('btn-esc-save-text').textContent  = 'Atualizar';
  abrirModal('modal-escala');
}

async function populateFreelancerSelect() {
  const freelancers = await FreelancerAPI.listar();
  const ativos = freelancers.filter(f => f.status === 'ativo');
  const sel = document.getElementById('esc-freelancer');
  sel.innerHTML = '<option value="">Selecione o freelancer...</option>' +
    ativos.map(f => `<option value="${f.id}" data-valor="${f.valorHora || 0}">${esc(f.nome)} — ${esc(f.especialidade || '—')}</option>`).join('');

  sel.addEventListener('change', calcularValorEscala);
}

// ── Calcular valor estimado ──
function calcularValorEscala() {
  const inicio  = document.getElementById('esc-hora-inicio').value;
  const fim     = document.getElementById('esc-hora-fim').value;
  const flSel   = document.getElementById('esc-freelancer');
  const option  = flSel.selectedOptions[0];
  const vHora   = option ? parseFloat(option.dataset.valor || 0) : 0;

  const dur = calcularDuracao(inicio, fim);

  document.getElementById('esc-duracao').textContent       = dur ? dur + 'h' : '—';
  document.getElementById('esc-valor-hora-display').textContent = vHora ? 'R$ ' + vHora.toFixed(2).replace('.', ',') : '—';

  if (dur && vHora) {
    const total = dur * vHora;
    document.getElementById('esc-total-display').textContent = 'R$ ' + total.toFixed(2).replace('.', ',');
    document.getElementById('esc-valor-total').value = total.toFixed(2);
  } else {
    document.getElementById('esc-total-display').textContent = '—';
    document.getElementById('esc-valor-total').value = '';
  }
}

// ── Salvar escala ──
async function salvarEscala() {
  clearModalAlert('modal-esc-alert');

  const data       = document.getElementById('esc-data').value;
  const freelId    = document.getElementById('esc-freelancer').value;
  const horaInicio = document.getElementById('esc-hora-inicio').value;
  const horaFim    = document.getElementById('esc-hora-fim').value;

  let valid = true;
  if (!data)      { setFieldError('esc-data', true);       valid = false; } else setFieldError('esc-data', false);
  if (!freelId)   { setFieldError('esc-freelancer', true); valid = false; } else setFieldError('esc-freelancer', false);
  if (!horaInicio){ setFieldError('esc-hora-inicio', true); valid = false; } else setFieldError('esc-hora-inicio', false);
  if (!horaFim)   { setFieldError('esc-hora-fim', true);   valid = false; } else setFieldError('esc-hora-fim', false);

  if (!valid) { showModalAlert('modal-esc-alert', 'Preencha todos os campos obrigatórios.'); return; }

  if (horaFim <= horaInicio) {
    showModalAlert('modal-esc-alert', 'O horário de término deve ser após o início.');
    return;
  }

  // Java LocalTime exige formato "HH:MM:SS" — o input HTML retorna "HH:MM"
  const horaInicioFmt = horaInicio.length === 5 ? horaInicio + ':00' : horaInicio;
  const horaFimFmt    = horaFim.length    === 5 ? horaFim    + ':00' : horaFim;

  const dados = {
    data,
    freelancerId: parseInt(freelId),
    horaInicio:   horaInicioFmt,
    horaFim:      horaFimFmt,
    funcao:       document.getElementById('esc-funcao').value,
    status:       document.getElementById('esc-status').value,
    observacoes:  document.getElementById('esc-observacoes').value.trim(),
    valorTotal:   parseFloat(document.getElementById('esc-valor-total').value) || 0,
  };

  setBtnLoading('btn-salvar-escala', 'btn-esc-save-text', 'btn-esc-spinner', true);

  try {
    const id = document.getElementById('esc-id').value;
    if (id) {
      await EscalaAPI.atualizar(parseInt(id), dados);
      AuditService.log('ESCALA_EDITADA', { id: parseInt(id), data: dados.data, freelancerId: dados.freelancerId });
      showToast('Escala atualizada com sucesso!', 'success');
    } else {
      const created = await EscalaAPI.criar(dados);
      AuditService.log('ESCALA_CRIADA', { id: created?.id, data: dados.data, freelancerId: dados.freelancerId, valorTotal: dados.valorTotal });
      showToast('Escala cadastrada com sucesso!', 'success');
    }

    fecharModal('modal-escala');
    await renderEscalas();
    updateDashboard();
    renderPagamentos();
  } catch (err) {
    showModalAlert('modal-esc-alert', err.message || 'Erro ao salvar escala.');
  } finally {
    setBtnLoading('btn-salvar-escala', 'btn-esc-save-text', 'btn-esc-spinner', false);
  }
}

function confirmarExclusaoEscala(id) {
  document.getElementById('confirm-title').textContent = 'Excluir escala?';
  document.getElementById('confirm-msg').textContent   = 'Esta ação não pode ser desfeita.';

  const btn = document.getElementById('btn-confirmar-delete');
  btn.onclick = async () => {
    fecharModal('modal-confirmar');
    try {
      const esc = (await EscalaAPI.listar()).find(e => e.id === id) || {};
      await EscalaAPI.excluir(id);
      AuditService.log('ESCALA_EXCLUIDA', { id, data: esc.data, freelancerId: esc.freelancerId });
      showToast('Escala excluída.', 'success');
      await renderEscalas();
      updateDashboard();
      renderPagamentos();
    } catch (err) {
      showToast(err.message || 'Erro ao excluir.', 'error');
    }
  };

  abrirModal('modal-confirmar');
}

function limparFormEscala() {
  document.getElementById('form-escala').reset();
  document.getElementById('esc-id').value = '';
  document.getElementById('esc-duracao').textContent = '—';
  document.getElementById('esc-valor-hora-display').textContent = '—';
  document.getElementById('esc-total-display').textContent = '—';
  clearModalAlert('modal-esc-alert');
  document.querySelectorAll('#form-escala .form-group').forEach(g => g.classList.remove('has-error'));
}

// ══════════════════════════════════════════════════════════════
// PAGAMENTOS
// ══════════════════════════════════════════════════════════════

async function renderPagamentos() {
  const escalas     = await EscalaAPI.listar();
  const freelancers = await FreelancerAPI.listar();

  const confirmadas = escalas.filter(e => e.status === 'confirmado');
  const pagamentos  = lsGet('rc_pagamentos');

  const totalPago    = pagamentos.filter(p => p.statusPagamento === 'pago').reduce((s, p) => s + (p.total || 0), 0);
  const totalPendente = confirmadas.reduce((s, e) => s + (parseFloat(e.valorTotal) || 0), 0);

  document.getElementById('pag-pago').textContent     = 'R$ ' + totalPago.toFixed(2).replace('.', ',');
  document.getElementById('pag-pendente').textContent = 'R$ ' + totalPendente.toFixed(2).replace('.', ',');
  document.getElementById('pag-escalas').textContent  = confirmadas.length;
  document.getElementById('pag-freelancers').textContent = freelancers.filter(f => f.status === 'ativo').length;

  const tbody = document.getElementById('pag-tbody');

  if (confirmadas.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7"><div class="empty-state">
      <div class="empty-icon">💰</div>
      <h4>Nenhum pagamento registrado</h4>
      <p>Confirme escalas para gerar registros de pagamento</p>
    </div></td></tr>`;
    return;
  }

  tbody.innerHTML = confirmadas.map(e => {
    const fl = freelancers.find(f => f.id === e.freelancerId) || {};
    const dur = calcularDuracao(e.horaInicio, e.horaFim);
    const pag = pagamentos.find(p => p.escalaId === e.id);
    const statusPag = pag?.statusPagamento || 'pendente';

    return `<tr>
      <td><div class="person-cell">
        <div class="avatar ${avatarColor(fl.id)}">${initials(fl.nome || '?')}</div>
        <div>
          <div class="person-name">${esc(fl.nome || '—')}</div>
          <div class="person-sub">${esc(fl.especialidade || '—')}</div>
        </div>
      </div></td>
      <td>${formatDate(e.data)} · ${e.horaInicio || '--'}–${e.horaFim || '--'}</td>
      <td>${dur ? dur + 'h' : '—'}</td>
      <td>${fl.valorHora ? 'R$ ' + parseFloat(fl.valorHora).toFixed(2).replace('.', ',') : '—'}</td>
      <td class="fw-bold">${e.valorTotal ? 'R$ ' + parseFloat(e.valorTotal).toFixed(2).replace('.', ',') : '—'}</td>
      <td><span class="badge ${statusPag === 'pago' ? 'badge-success' : 'badge-warning'}">${statusPag}</span></td>
      <td>
        <div class="table-actions">
          ${statusPag !== 'pago'
            ? `<button class="btn btn-sm btn-success" onclick="confirmarPagamento(${e.id})">
                 <i class="fa fa-check"></i> Pagar
               </button>`
            : '<span class="text-success text-sm fw-semi"><i class="fa fa-check-circle"></i> Pago</span>'}
        </div>
      </td>
    </tr>`;
  }).join('');
}

// ── Confirmar pagamento (com modal de dupla confirmação) ──
async function confirmarPagamento(escalaId) {
  const escalas     = await EscalaAPI.listar();
  const freelancers = await FreelancerAPI.listar();
  const e  = escalas.find(e => e.id === escalaId) || {};
  const fl = freelancers.find(f => f.id === e.freelancerId) || {};
  const dur   = calcularDuracao(e.horaInicio, e.horaFim);
  const total = e.valorTotal ? 'R$ ' + parseFloat(e.valorTotal).toFixed(2).replace('.', ',') : '—';

  document.getElementById('pag-confirm-nome').textContent =
    `Confirmar pagamento para ${fl.nome || 'freelancer'}?`;
  document.getElementById('pag-confirm-detalhe').textContent =
    'Esta ação registrará o pagamento e não poderá ser revertida.';

  document.getElementById('pag-confirm-box').innerHTML = `
    <div><strong>Data:</strong> ${formatDate(e.data)}</div>
    <div><strong>Período:</strong> ${e.horaInicio || '--'}–${e.horaFim || '--'}${dur ? ' (' + dur + 'h)' : ''}</div>
    <div><strong>Função:</strong> ${esc(e.funcao || fl.especialidade || '—')}</div>
    <div><strong>Valor total:</strong> <span class="detail-value-highlight">${total}</span></div>
  `;

  const btn = document.getElementById('btn-confirmar-pagamento');
  btn.onclick = () => {
    fecharModal('modal-confirmar-pag');
    executarPagamento(escalaId, fl.nome, e.valorTotal);
  };

  abrirModal('modal-confirmar-pag');
}

async function executarPagamento(escalaId, nomeFreelancer, valorTotal) {
  try {
    let pagamentos = lsGet('rc_pagamentos');
    const idx = pagamentos.findIndex(p => p.escalaId === escalaId);
    if (idx >= 0) {
      pagamentos[idx].statusPagamento = 'pago';
      pagamentos[idx].dataPagamento   = new Date().toISOString();
    } else {
      pagamentos.push({
        id: nextId(pagamentos),
        escalaId,
        statusPagamento: 'pago',
        dataPagamento:   new Date().toISOString(),
      });
    }
    lsSet('rc_pagamentos', pagamentos);
    AuditService.log('PAGAMENTO_REGISTRADO', {
      escalaId,
      freelancer: nomeFreelancer || '—',
      valor:      valorTotal     || 0,
    });
    showToast('Pagamento registrado com sucesso!', 'success');
    renderPagamentos();
  } catch (err) {
    showToast('Erro ao registrar pagamento.', 'error');
  }
}

// ══════════════════════════════════════════════════════════════
// AUDITORIA
// ══════════════════════════════════════════════════════════════

function renderAuditoria() {
  const filtroAcao = document.getElementById('aud-filter-acao')?.value || '';
  const logs       = AuditService.getLogs(filtroAcao);

  // Stats
  const hoje     = new Date().toISOString().slice(0, 10);
  const logsHoje = logs.filter(l => l.timestamp.startsWith(hoje));
  const usuarios  = new Set(logs.map(l => l.usuario)).size;

  document.getElementById('aud-total').textContent    = logs.length;
  document.getElementById('aud-hoje').textContent     = logsHoje.length;
  document.getElementById('aud-usuarios').textContent = usuarios;

  // Badge sidebar
  const badge = document.getElementById('badge-auditoria');
  if (badge) badge.textContent = logsHoje.length || '';

  const tbody = document.getElementById('aud-tbody');
  if (!logs.length) {
    tbody.innerHTML = `<tr><td colspan="4">
      <div class="empty-state">
        <div class="empty-icon">🔍</div>
        <h4>Nenhum registro encontrado</h4>
        <p>As ações do sistema aparecerão aqui</p>
      </div></td></tr>`;
    return;
  }

  tbody.innerHTML = logs.map(l => {
    const meta   = (AUDIT_LABELS && AUDIT_LABELS[l.acao]) || { label: l.acao, icon: '📋', cor: 'badge-neutral' };
    const dt     = new Date(l.timestamp);
    const data   = dt.toLocaleDateString('pt-BR');
    const hora   = dt.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const detalhe = Object.entries(l.detalhe || {})
      .map(([k, v]) => `<span style="margin-right:8px;"><strong>${esc(k)}:</strong> ${esc(String(v))}</span>`)
      .join('');
    return `<tr>
      <td class="audit-timestamp">${data}<span class="audit-time">${hora}</span></td>
      <td style="font-size:0.83rem; color:var(--text-mid);">${esc(l.usuario)}</td>
      <td>
        <div class="audit-acao-cell">
          <span style="font-size:1.0rem;">${meta.icon}</span>
          <span class="badge ${meta.cor}">${esc(meta.label)}</span>
        </div>
      </td>
      <td><div class="audit-detail">${detalhe || '<span style="color:var(--text-light);">—</span>'}</div></td>
    </tr>`;
  }).join('');
}

// ══════════════════════════════════════════════════════════════
// UTILITÁRIOS
// ══════════════════════════════════════════════════════════════

function calcularDuracao(inicio, fim) {
  if (!inicio || !fim) return null;
  const [h1, m1] = inicio.split(':').map(Number);
  const [h2, m2] = fim.split(':').map(Number);
  const mins = (h2 * 60 + m2) - (h1 * 60 + m1);
  if (mins <= 0) return null;
  return parseFloat((mins / 60).toFixed(1));
}

function formatDate(iso) {
  if (!iso) return '—';
  const [y, m, d] = iso.split('-');
  return `${d}/${m}/${y}`;
}

function statusBadge(status) {
  const map = {
    confirmado: 'badge-success',
    pendente:   'badge-warning',
    cancelado:  'badge-error'
  };
  return map[status] || 'badge-neutral';
}

function initials(nome) {
  if (!nome) return '?';
  return nome.trim().split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase();
}

function avatarColor(id) {
  const colors = ['av-1', 'av-2', 'av-3', 'av-4', 'av-5', 'av-6', 'av-7'];
  return colors[(id || 0) % colors.length];
}

function esc(str) {
  return String(str || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function setBtnLoading(btnId, textId, spinnerId, on) {
  const btn = document.getElementById(btnId);
  if (btn) btn.disabled = on;
  const textEl = document.getElementById(textId);
  if (textEl) textEl.textContent = on ? 'Salvando...' : textEl.dataset.orig || textEl.textContent;
  const spinEl = document.getElementById(spinnerId);
  if (spinEl) spinEl.classList.toggle('hidden', !on);
}

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
