// session.js (renovación manual + header unificado)
// - Muestra contador #sessionCountdown si hay sesión.
// - Muestra botón #renewBtn y #logoutBtn cuando hay sesión; muestra #loginLink cuando no.
// - Oculta/enseña enlaces [data-requires-role="ADMIN"] según el rol del usuario.
// - Sin modales automáticos.

import API from './api.js';
import { qs, qsa, showToast } from './util.js';

let tick = null;
let expiresAt = null;

const COUNTDOWN_ID = 'sessionCountdown';
const RENEW_BTN_ID = 'renewBtn';
const LOGOUT_BTN_ID = 'logoutBtn';
const LOGIN_LINK_ID = 'loginLink';

function hydrateSessionUI() {
    const token = API.token();
    const counter = qs('#' + COUNTDOWN_ID);

    // Limpia interval previo
    if (tick) { clearInterval(tick); tick = null; }

    // Si no hay token: aplicar header sin sesión y salir
    if (!token) {
        applyHeader(null);
        counter && counter.classList.add('hidden');
        return;
    }

    // Con token: traemos /me para rol y expiración
    bootstrapMe().then(me => {
        applyHeader(me); // muestra/oculta botones y enlaces ADMIN

        // Manejo de contador si viene expiresAt
        if (!me?.expiresAt) {
            counter && counter.classList.add('hidden');
            return;
        }
        expiresAt = me.expiresAt;
        counter && counter.classList.remove('hidden');
        startCountdown(counter);
    }).catch(() => {
        // Si falla /me, asumir no autenticado
        applyHeader(null);
        counter && counter.classList.add('hidden');
    });

    // Wire de botones cada vez (idempotente)
    wireRenew();
    wireLogout();
}

async function bootstrapMe() {
    // Devuelve objeto { userId, username, role, expiresAt } o lanza si 401
    return await API.get('/api/auth/me');
}

function startCountdown(counterEl) {
    updateBadge(counterEl); // primer pintado
    tick = setInterval(() => {
        if (!expiresAt) return;
        updateBadge(counterEl);
    }, 1000);
}

function updateBadge(counterEl) {
    const diff = new Date(expiresAt).getTime() - Date.now();
    const secs = Math.floor(diff / 1000);
    if (secs <= 0) {
        counterEl.textContent = 'Expirada';
        return;
    }
    counterEl.textContent = formatRemaining(secs);
}

function formatRemaining(secs) {
    if (secs < 60) return secs + 's';
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${String(s).padStart(2,'0')}`;
}

// Botón Renovar
function wireRenew() {
    const renewBtn = qs('#' + RENEW_BTN_ID);
    if (!renewBtn) return;
    renewBtn.classList.remove('hidden');
    renewBtn.onclick = async () => {
        await renewSessionManual();
    };
}

// Botón Salir
function wireLogout() {
    const logoutBtn = qs('#' + LOGOUT_BTN_ID);
    if (!logoutBtn) return;
    logoutBtn.onclick = async () => {
        try {
            await API.post('/api/auth/logout', {});
        } catch (_) {
            // ignorar errores de logout
        }
        // Limpiar tokens y UI
        localStorage.removeItem('sessionToken');
        sessionStorage.removeItem('sessionToken');
        showToast('Sesión cerrada', 'success');
        applyHeader(null);
        // Redirige a login salvo que ya estés ahí
        if (!location.pathname.endsWith('/login.html')) {
            setTimeout(()=> location.href='login.html', 600);
        }
    };
}

// Aplica visibilidad en el header según sesión/rol
function applyHeader(me) {
    const hasSession = !!me;
    const loginLink = qs('#' + LOGIN_LINK_ID);
    const logoutBtn = qs('#' + LOGOUT_BTN_ID);
    const renewBtn = qs('#' + RENEW_BTN_ID);
    const counter = qs('#' + COUNTDOWN_ID);

    if (hasSession) {
        loginLink && loginLink.classList.add('hidden');
        logoutBtn && logoutBtn.classList.remove('hidden');
        renewBtn && renewBtn.classList.remove('hidden');
        counter && counter.classList.remove('hidden');
    } else {
        loginLink && loginLink.classList.remove('hidden');
        logoutBtn && logoutBtn.classList.add('hidden');
        renewBtn && renewBtn.classList.add('hidden');
        counter && counter.classList.add('hidden');
    }

    // Enlaces solo-ADMIN
    const adminEls = qsa('[data-requires-role="ADMIN"]');
    const isAdmin = me?.role === 'ADMIN';
    adminEls.forEach(el => {
        if (isAdmin) el.classList.remove('hidden');
        else el.classList.add('hidden');
    });
}

// Renovación manual
async function renewSessionManual() {
    try {
        const d = await API.post('/api/auth/renew', {});
        showToast('Sesión renovada', 'success');
        const ttl = Number(d?.expiresIn || 0);
        if (ttl > 0) {
            const now = Date.now();
            expiresAt = new Date(now + ttl * 1000).toISOString();
            const counter = qs('#' + COUNTDOWN_ID);
            if (counter) {
                counter.classList.remove('hidden');
                if (tick) clearInterval(tick);
                startCountdown(counter);
            }
        }
    } catch (err) {
        showToast('No se pudo renovar la sesión', 'error');
    }
}

export {
    hydrateSessionUI,
    renewSessionManual
};