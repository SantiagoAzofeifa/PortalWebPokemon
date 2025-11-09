// session.js
import API from './api.js';
import {qs, qsa, showToast} from './util.js';

let countdownTimer = null;

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
        logoutBtn.onclick = async ()=>{
            try { await API.post('/api/auth/logout',{}); } catch{}
            localStorage.removeItem('sessionToken');
            showToast('Sesión cerrada','info');
            location.href='login.html';
        };
    }).catch(()=>{
        localStorage.removeItem('sessionToken');
        hydrateSessionUI();
    });
}

function applyRoleVisibility(role) {
    qsa('[data-requires-role]').forEach(el=>{
        const need = el.getAttribute('data-requires-role');
        if (!role || role !== need) el.classList.add('hidden'); else el.classList.remove('hidden');
    });
}

function startCountdown(expiresAt) {
    const el = qs('#sessionCountdown');
    if (!el) return;
    if (countdownTimer) clearInterval(countdownTimer);
    countdownTimer = setInterval(()=>{
        const diff = (new Date(expiresAt).getTime() - Date.now())/1000;
        if (diff <= 0) {
            el.textContent = 'Expirada';
            el.style.color='var(--danger)';
            clearInterval(countdownTimer);
            return;
        }
        el.textContent = `Expira en ${Math.floor(diff)}s`;
        if (diff < 30) el.style.color='var(--danger)';
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