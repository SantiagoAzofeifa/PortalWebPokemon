import API from './api.js';
import { qs, showToast } from './util.js';

function initAuthForms() {
    const regForm = qs('#registerForm');
    const loginForm = qs('#loginForm');
    const renewBtn = qs('#renewBtn');
    const out = qs('#authOutput');

    if (regForm) {
        regForm.onsubmit = async (e)=>{
            e.preventDefault();
            const fd = new FormData(regForm);
            const body = Object.fromEntries(fd.entries());
            try {
                await API.post('/api/auth/register', body);
                showToast('Registro exitoso','success');
                regForm.reset();
            } catch {}
        };
    }

    if (loginForm) {
        loginForm.onsubmit = async (e)=>{
            e.preventDefault();
            const fd = new FormData(loginForm);
            const body = Object.fromEntries(fd.entries());
            try {
                const data = await API.post('/api/auth/login', body);
                localStorage.setItem('sessionToken', data.token);
                showToast(`Bienvenido ${data.username}`,'success');
                renewBtn.classList.remove('hidden');
                out.textContent = JSON.stringify(data,null,2);
                setTimeout(()=> location.href='index.html',800);
            } catch (err) {
                out.textContent = err.message;
            }
        };
    }

    if (renewBtn) {
        renewBtn.onclick = async ()=>{
            try {
                const d = await API.post('/api/auth/renew',{});
                showToast('Sesión renovada','success');
                out.textContent = JSON.stringify(d,null,2);
            } catch {}
        };
    }
}

export { initAuthForms };