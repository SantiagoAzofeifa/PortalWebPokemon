import API from './api.js';
import { qs, showToast } from './util.js';

let countdownTimer = null;
// Renovación automática DESACTIVADA
const autoRenewEnabled = false;
const WARN_THRESHOLD = 45; // segundos para advertencia visual
let warned = false;

function hydrateSessionUI() {
    const token = API.token();
    const loginLink = qs('#loginLink');
    const logoutBtn = qs('#logoutBtn');
    const renewBtn = qs('#renewBtn');
    const userInfo = qs('#userInfo');

    if (!token) {
        if (loginLink) loginLink.classList.remove('hidden');
        if (logoutBtn) logoutBtn.classList.add('hidden');
        if (renewBtn) renewBtn.classList.add('hidden');
        hideCountdown();
        applyRoleVisibility(null);
        return;
    }

    // Asegurar que exista UI para countdown en esta página (nav o widget flotante)
    ensureSessionWidgets();

    API.get('/api/auth/me').then(d=>{
        if (loginLink) loginLink.classList.add('hidden');
        if (logoutBtn) logoutBtn.classList.remove('hidden');
        if (renewBtn) renewBtn.classList.remove('hidden');
        if (userInfo) userInfo.textContent = `${d.username} (${d.role})`;
        applyRoleVisibility(d.role);
        warned = false;
        showCountdown();
        startCountdown(d.expiresAt);

        if (logoutBtn) {
            logoutBtn.onclick = async ()=>{
                try { await API.post('/api/auth/logout',{}); } catch {}
                localStorage.removeItem('sessionToken');
                showToast('Sesión cerrada','info');
                location.href='login.html';
            };
        }

        bindRenewButtons();

    }).catch(err=>{
        console.warn('[SESSION] fallo /api/auth/me ->', err?.message||err);
        showToast('Sesión inválida o expirada. Inicia sesión nuevamente.','error');
        setTimeout(()=> {
            localStorage.removeItem('sessionToken');
            location.href='login.html';
        }, 1200);
    });
}

function applyRoleVisibility(role) {
    document.querySelectorAll('[data-requires-role]').forEach(el=>{
        const need = (el.getAttribute('data-requires-role')||'').trim().toUpperCase();
        const cur = (role||'').trim().toUpperCase();
        el.classList.toggle('hidden', !(cur && cur===need));
    });
}

function startCountdown(expiresAt) {
    const navEl = qs('#sessionCountdown');
    const floEl = qs('#sessionCountdownFloating');
    clearInterval(countdownTimer);
    countdownTimer = setInterval(()=>{
        const diff = (new Date(expiresAt).getTime() - Date.now())/1000;
        const text = diff <= 0 ? 'Expirada' : `Expira en ${Math.floor(diff)}s`;

        if (navEl) {
            navEl.textContent = text;
            navEl.classList.add('badge');
            if (diff <= 0) {
                navEl.classList.remove('accent','warn'); navEl.classList.add('danger');
            } else if (diff < WARN_THRESHOLD) {
                navEl.classList.remove('accent','danger'); navEl.classList.add('warn');
                if (!warned && !autoRenewEnabled) {
                    warned = true;
                    showToast('Tu sesión está por expirar. Usa "Renovar" para extender.','warn', 5000);
                }
            } else {
                navEl.classList.remove('warn','danger'); navEl.classList.add('accent');
            }
        }
        if (floEl) {
            floEl.textContent = text;
            floEl.classList.add('badge');
            // Colores básicos en el widget
            if (diff <= 0) {
                floEl.style.background = 'var(--danger)';
            } else if (diff < WARN_THRESHOLD) {
                floEl.style.background = 'var(--warn)';
            } else {
                floEl.style.background = 'var(--accent)';
            }
        }

        if (diff <= 0) {
            clearInterval(countdownTimer);
        }
    }, 1000);
}

function showCountdown() {
    const el = qs('#sessionCountdown');
    if (el) el.classList.remove('hidden');
    const flo = qs('#sessionFloating');
    if (flo) flo.style.display = 'flex';
}
function hideCountdown() {
    const el = qs('#sessionCountdown');
    if (el) el.classList.add('hidden');
    const flo = qs('#sessionFloating');
    if (flo) flo.style.display = 'none';
}

function ensureSessionWidgets() {
    // Si no hay contador en nav, crea un widget flotante con botón "Renovar"
    if (!qs('#sessionCountdown') && !qs('#sessionFloating')) {
        const wrap = document.createElement('div');
        wrap.id = 'sessionFloating';
        wrap.style.cssText = 'position:fixed; right:12px; bottom:12px; z-index:3000; display:flex; gap:.5rem; align-items:center; background:var(--panel); border:1px solid var(--border); padding:.45rem .6rem; border-radius:12px; box-shadow:var(--shadow-sm);';
        wrap.innerHTML = `
      <span id="sessionCountdownFloating" class="badge accent" aria-live="polite">—</span>
      <button id="renewBtnFloating" class="btn small outline" type="button">Renovar</button>
    `;
        document.body.appendChild(wrap);
    }
}

function bindRenewButtons() {
    // Botón en nav
    const renewBtn = qs('#renewBtn');
    if (renewBtn) renewBtn.onclick = renewSessionManual;
    // Botón en widget flotante
    const renewFlo = qs('#renewBtnFloating');
    if (renewFlo) renewFlo.onclick = renewSessionManual;
}

async function renewSessionManual() {
    try {
        const d = await API.post('/api/auth/renew',{});
        showToast('Sesión renovada','success');
        const seconds = d?.expiresIn ? Number(d.expiresIn) : 180;
        const newExpISO = new Date(Date.now() + seconds*1000).toISOString();
        showCountdown();
        startCountdown(newExpISO);
    } catch {
        showToast('No se pudo renovar. Inicia sesión.','error');
        setTimeout(()=> location.href='login.html', 1000);
    }
}

export { hydrateSessionUI, renewSessionManual };