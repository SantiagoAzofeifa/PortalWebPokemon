// session.js
// Manejo centralizado de sesión con modal de renovación automática.
// Requiere: API helper (api.js), util.js (showToast), y el modal HTML incluido en cada página.

import API from './api.js';
import { qs, showToast } from './util.js';

/**
 * Estado interno de la sesión.
 * expiresAt: ISO string del momento de expiración (cuando se creó el token)
 * remainingSecs: número aproximado para display (calculado)
 */
let sessionState = {
    expiresAt: null,
    remainingSecs: 0,
    tickInterval: null,
    modalVisible: false
};

const COUNTDOWN_ELEMENT_ID = 'sessionCountdown';
const CHECK_INTERVAL_MS = 1000;
const MODAL_ID = 'sessionRenewModal';

function hydrateSessionUI() {
    // Si no hay token, ocultar contador y terminar.
    if (!API.token()) {
        const el = qs('#' + COUNTDOWN_ELEMENT_ID);
        el && el.classList.add('hidden');
        return;
    }

    // Intentamos obtener datos de sesión (/api/auth/me) para saber expiración.
    // El login response original trae expiresIn (segundos) pero aquí podemos
    // recalcular a partir de la expiración persistida en memoria si la incluiste;
    // tu /api/auth/me devuelve expiresAt (Instant string).
    bootstrapExpiration().then(() => {
        startCountdownLoop();
    }).catch(() => {
        // Si falla, asumimos sesión inválida y limpiamos.
        handleSessionExpired();
    });

    wireRenewModal();
}

/**
 * Inicializa la expiración preguntando /api/auth/me.
 * Espera un campo expiresAt (ISO), si no existe se estima usando un TTL fallback.
 */
async function bootstrapExpiration() {
    const data = await API.get('/api/auth/me');
    if (!data.expiresAt) {
        // Si tu backend no manda expiresAt, reemplaza por Date.now()+ ttl*1000 (si conservas el TTL tras login).
        // Por ahora, si no viene, se fuerza a 2 minutos.
        const now = Date.now();
        sessionState.expiresAt = new Date(now + 120000).toISOString();
    } else {
        sessionState.expiresAt = data.expiresAt;
    }
}

/**
 * Loop que actualiza el contador y muestra modal cuando expira.
 */
function startCountdownLoop() {
    clearInterval(sessionState.tickInterval);

    sessionState.tickInterval = setInterval(() => {
        if (!sessionState.expiresAt) return;

        const diffMs = new Date(sessionState.expiresAt).getTime() - Date.now();
        const remaining = Math.floor(diffMs / 1000);
        sessionState.remainingSecs = remaining;

        updateCountdownBadge(remaining);

        if (remaining <= 0) {
            // Expirada: detener loop y mostrar modal si no se ha mostrado.
            clearInterval(sessionState.tickInterval);
            showRenewModal();
        }
    }, CHECK_INTERVAL_MS);
}

/**
 * Actualiza el badge visual de cuenta regresiva si existe.
 */
function updateCountdownBadge(secs) {
    const el = qs('#' + COUNTDOWN_ELEMENT_ID);
    if (!el) return;
    if (secs <= 0) {
        el.textContent = 'Expirada';
        el.classList.remove('hidden');
        return;
    }
    el.classList.remove('hidden');
    el.textContent = formatRemaining(secs);
}

/**
 * Aplica formato amigable: mm:ss o ss si < 60.
 */
function formatRemaining(secs) {
    if (secs < 60) return secs + 's';
    const m = Math.floor(secs / 60);
    const s = secs % 60;
    return `${m}:${String(s).padStart(2,'0')}`;
}

/**
 * Muestra el modal de renovar sesión.
 */
function showRenewModal() {
    if (sessionState.modalVisible) return;
    const modal = qs('#' + MODAL_ID);
    if (!modal) return;
    modal.classList.remove('hidden');
    modal.setAttribute('aria-hidden','false');
    sessionState.modalVisible = true;
}

/**
 * Oculta el modal (post renovación o logout).
 */
function hideRenewModal() {
    const modal = qs('#' + MODAL_ID);
    if (!modal) return;
    modal.classList.add('hidden');
    modal.setAttribute('aria-hidden','true');
    sessionState.modalVisible = false;
}

/**
 * Wire de los botones del modal.
 */
function wireRenewModal() {
    const yesBtn = qs('#sessionRenewYes');
    const noBtn = qs('#sessionRenewNo');
    if (yesBtn) {
        yesBtn.onclick = async () => {
            try {
                const resp = await API.post('/api/auth/renew', {}); // backend renueva
                if (!resp || !resp.expiresIn) {
                    // Si /renew no devuelve expiresAt, recalculamos.
                    const now = Date.now();
                    const ttlMs = resp?.expiresIn ? resp.expiresIn * 1000 : 60000;
                    sessionState.expiresAt = new Date(now + ttlMs).toISOString();
                } else {
                    // Si decidieras enviar expiresAt en este endpoint, úsalo directamente.
                    const now = Date.now();
                    const ttlMs = resp.expiresIn * 1000;
                    sessionState.expiresAt = new Date(now + ttlMs).toISOString();
                }
                hideRenewModal();
                showToast('Sesión renovada','success');
                startCountdownLoop();
            } catch (err) {
                showToast('No se pudo renovar la sesión','error');
                handleSessionExpired(); // si falla, se cierra la sesión
            }
        };
    }
    if (noBtn) {
        noBtn.onclick = () => {
            handleSessionExpired();
        };
    }
}

/**
 * Limpia token, muestra mensaje y redirige.
 */
function handleSessionExpired() {
    localStorage.removeItem('sessionToken');
    hideRenewModal();
    showToast('Sesión finalizada','warn');
    // Puedes usar setTimeout para mostrar el toast unos ms antes de redirigir
    setTimeout(()=> location.href='login.html', 800);
}

/**
 * Para uso opcional desde login.js si quieres forzar un rebootstrap tras login.
 */
function restartSessionCountdown(ttlSeconds) {
    const now = Date.now();
    sessionState.expiresAt = new Date(now + ttlSeconds*1000).toISOString();
    startCountdownLoop();
}

export {
    hydrateSessionUI,
    restartSessionCountdown
};