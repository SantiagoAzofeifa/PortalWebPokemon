import API from './api.js';
import { qs, showToast } from './util.js';

let countdownTimer = null;
let autoRenewEnabled = true; // puedes apagarlo si no deseas renovación automática
const RENEW_THRESHOLD = 30;  // renovar si faltan menos de 30s

function hydrateSessionUI() {
    const token = API.token();
    const loginLink = qs('#loginLink');
    const logoutBtn = qs('#logoutBtn');
    const userInfo = qs('#userInfo');
    if (!token) {
        if (loginLink) loginLink.classList.remove('hidden');
        if (logoutBtn) logoutBtn.classList.add('hidden');
        if (userInfo) userInfo.textContent = 'No autenticado';
        applyRoleVisibility(null);
        return;
    }
    API.get('/api/auth/me').then(d=>{
        if (loginLink) loginLink.classList.add('hidden');
        if (logoutBtn) logoutBtn.classList.remove('hidden');
        if (userInfo) userInfo.textContent = `${d.username} (${d.role})`;
        applyRoleVisibility(d.role);
        startCountdown(d.expiresAt);
        if (logoutBtn) {
            logoutBtn.onclick = async ()=>{
                try { await API.post('/api/auth/logout',{}); } catch{}
                localStorage.removeItem('sessionToken');
                showToast('Sesión cerrada','info');
                location.href='login.html';
            };
        }
    }).catch(err=>{
        // NO borrar inmediatamente el token; permitir reintento o renovación manual
        console.warn('[SESSION] fallo /api/auth/me ->', err.message);
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
    const el = qs('#sessionCountdown');
    if (!el) return;
    clearInterval(countdownTimer);
    countdownTimer = setInterval(async ()=>{
        const diff = (new Date(expiresAt).getTime() - Date.now())/1000;
        if (diff <= 0) {
            el.textContent = 'Expirada';
            el.style.color='var(--danger)';
            clearInterval(countdownTimer);
            return;
        }
        el.textContent = `Expira en ${Math.floor(diff)}s`;
        el.style.color = diff < 30 ? 'var(--danger)' : '';
        if (autoRenewEnabled && diff < RENEW_THRESHOLD) {
            // Intento de renovación silenciosa
            clearInterval(countdownTimer);
            try {
                const d = await API.post('/api/auth/renew',{});
                showToast('Sesión renovada','success');
                startCountdown(new Date(Date.now()+d.expiresIn*1000).toISOString());
            } catch {
                showToast('Renovación falló, reautentica.','error');
            }
        }
    },1000);
}

async function renewSessionManual() {
    try {
        const d = await API.post('/api/auth/renew',{});
        showToast('Sesión renovada','success');
        startCountdown(new Date(Date.now()+d.expiresIn*1000).toISOString());
    } catch {}
}

export { hydrateSessionUI, renewSessionManual };