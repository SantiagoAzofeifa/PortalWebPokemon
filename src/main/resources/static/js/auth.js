// auth.js
import API from './api.js';
import { qs, showToast } from './util.js';

function initAuthPage() {
    const regForm = qs('#registerForm');
    const loginForm = qs('#loginForm');
    const renewBtn = qs('#renewBtn');
    const output = qs('#authOutput');

    // Registro
    regForm && (regForm.onsubmit = async e=>{
        e.preventDefault();
        const fd=new FormData(regForm);
        const body=Object.fromEntries(fd.entries());
        try {
            await API.post('/api/auth/register', body);
            showToast('Registro exitoso','success');
            regForm.reset();
        } catch (err) {
            output.textContent = err.message || 'Error en registro';
        }
    });

    // Login
    loginForm && (loginForm.onsubmit = async e=>{
        e.preventDefault();
        const fd=new FormData(loginForm);
        const body=Object.fromEntries(fd.entries());
        try {
            const r = await API.post('/api/auth/login', body);
            localStorage.setItem('sessionToken', r.token);
            output.textContent = JSON.stringify(r,null,2);
            showToast(`Bienvenido ${r.username}`,'success');
            // Línea eliminada: restartSessionCountdown(r.expiresIn);
            renewBtn && renewBtn.classList.remove('hidden');
            setTimeout(()=> location.href='index.html', 800);
        } catch (err) {
            output.textContent = err.message || 'Credenciales inválidas';
        }
    });

    // Botón renovar sesión (manual)
    renewBtn && (renewBtn.onclick = async ()=>{
        try {
            const d = await API.post('/api/auth/renew',{});
            showToast('Sesión renovada','success');
            output.textContent = JSON.stringify(d,null,2);
            // Si quieres refrescar el contador inmediatamente:
            // Puedes forzar un re-hydrate llamando a location.reload() o a hydrateSessionUI()
        } catch (err) {
            showToast('No se pudo renovar','error');
        }
    });
}

export { initAuthPage };