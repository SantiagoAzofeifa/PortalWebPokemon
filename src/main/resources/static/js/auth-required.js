// auth-required.js
import API from './api.js';
import { showToast } from './util.js';

// Llama a esto en páginas que requieren sesión activa (checkout, etapas de orden, etc.)
export function requireAuthOnLoad() {
    const t = API.token();
    if (!t) {
        console.warn('[AUTH] No hay token en almacenamiento. Redirigiendo a login...');
        showToast('Debes iniciar sesión para continuar.', 'error');
        setTimeout(() => (location.href = 'login.html'), 600);
        return false;
    }
    return true;
}