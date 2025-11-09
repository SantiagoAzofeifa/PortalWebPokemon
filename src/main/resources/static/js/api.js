// api.js
import { showToast } from './util.js';

const API = {
    // Lee token de localStorage y, si no está, intenta en sessionStorage.
    token() {
        return localStorage.getItem('sessionToken') || sessionStorage.getItem('sessionToken') || null;
    },

    headers(json = true) {
        const h = {};
        if (json) h['Content-Type'] = 'application/json';
        const t = API.token();
        if (t) {
            h['X-SESSION-TOKEN'] = t;
        }
        return h;
    },

    async get(url) {
        return handle(await fetch(url, { headers: API.headers(false) }));
    },
    async post(url, body = {}) {
        // Debug: imprime si hay o no token antes del POST
        const t = API.token();
        if (!t) {
            console.warn('[API] POST sin token ->', url);
        }
        return handle(
            await fetch(url, { method: 'POST', headers: API.headers(true), body: JSON.stringify(body) })
        );
    },
    async put(url, body = {}) {
        const t = API.token();
        if (!t) {
            console.warn('[API] PUT sin token ->', url);
        }
        return handle(
            await fetch(url, { method: 'PUT', headers: API.headers(true), body: JSON.stringify(body) })
        );
    },
    async del(url) {
        const t = API.token();
        if (!t) {
            console.warn('[API] DELETE sin token ->', url);
        }
        return handle(await fetch(url, { method: 'DELETE', headers: API.headers(false) }));
    }
};

async function handle(res) {
    const ct = res.headers.get('content-type') || '';
    let data;
    try {
        if (ct.includes('application/json')) data = await res.json();
        else data = await res.text();
    } catch {
        data = null;
    }

    if (!res.ok) {
        // Manejo especial 401: forzar login
        if (res.status === 401) {
            const message =
                data && data.error ? data.error : 'No autenticado. Por favor, inicia sesión de nuevo.';
            showToast(message, 'error');
            // Redirige a login y evita loops si ya estás ahí
            if (!location.pathname.endsWith('/login.html')) {
                setTimeout(() => (location.href = 'login.html'), 600);
            }
            throw new Error(message);
        }

        const msg =
            data && data.error
                ? data.error
                : typeof data === 'string'
                    ? data
                    : 'Error en la solicitud';
        showToast(msg, 'error');
        throw new Error(msg);
    }

    return data;
}

export default API;