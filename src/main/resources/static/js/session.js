import API from './api.js';
import { qs, showToast } from './util.js';

let countdownInterval = null;

function hydrateUserInfo() {
    const out = qs('#userInfo');
    const loginLink = qs('#loginLink');
    const logoutBtn = qs('#logoutBtn');
    const token = API.token();
    if (!token) {
        if (out) out.textContent = 'No autenticado';
        if (logoutBtn) logoutBtn.classList.add('hidden');
        if (loginLink) loginLink.classList.remove('hidden');
        return;
    }
    API.get('/api/auth/me')
        .then(data => {
            if (out) out.textContent = `${data.username} (${data.role})`;
            if (logoutBtn) logoutBtn.classList.remove('hidden');
            if (loginLink) loginLink.classList.add('hidden');
            applyRoleVisibility(data.role);
        })
        .catch(()=> {
            logoutLocal();
            hydrateUserInfo();
        });
    if (logoutBtn) {
        logoutBtn.onclick = async () => {
            try {
                await API.post('/api/auth/logout',{});
            } catch {}
            logoutLocal();
            location.href='login.html';
        };
    }
}

function logoutLocal() {
    localStorage.removeItem('sessionToken');
    showToast('Sesión terminada','info');
}

function applyRoleVisibility(role) {
    document.querySelectorAll('[data-requires-role]').forEach(el => {
        const needs = el.getAttribute('data-requires-role');
        if (needs !== role) {
            el.classList.add('hidden');
        } else {
            el.classList.remove('hidden');
        }
    });
}

function startSessionCountdown(targetId) {
    if (countdownInterval) clearInterval(countdownInterval);
    const el = qs(`#${targetId}`);
    if (!el) return;
    const token = API.token();
    if (!token) { el.textContent = '—'; return; }
    API.get('/api/auth/me').then(data => {
        updateCountdown(el, data.expiresAt);
        countdownInterval = setInterval(()=> updateCountdown(el, data.expiresAt), 1000);
    }).catch(()=> el.textContent='Expirada');
}

function updateCountdown(el, expiresAt) {
    const diff = (new Date(expiresAt).getTime() - Date.now())/1000;
    if (diff <= 0) {
        el.textContent = 'Sesión expirada';
        el.classList.add('warning');
        return;
    }
    el.textContent = `Expira en ${Math.floor(diff)}s`;
    if (diff < 30) el.style.color='var(--color-danger)';
}

export { hydrateUserInfo, startSessionCountdown };