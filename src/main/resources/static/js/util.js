// Utilidades genéricas

function qs(sel, ctx=document){ return ctx.querySelector(sel); }
function qsa(sel, ctx=document){ return [...ctx.querySelectorAll(sel)]; }

function showToast(message, type='info', timeout=4000) {
    const c = qs('#toastContainer');
    if (!c) return;
    const d = document.createElement('div');
    d.className = `toast ${type==='error'?'error': type==='success'?'success':''}`;
    d.setAttribute('role','alert');
    d.textContent = message;
    c.appendChild(d);
    setTimeout(()=> d.remove(), timeout);
}

function initThemeToggle() {
    const btn = qs('#themeToggle');
    if (!btn) return;
    btn.addEventListener('click', () => {
        document.body.classList.toggle('theme-dark');
        localStorage.setItem('theme', document.body.classList.contains('theme-dark') ? 'dark':'light');
    });
    const saved = localStorage.getItem('theme');
    if (saved === 'dark') document.body.classList.add('theme-dark');
}

function formatMoney(v) {
    return '$' + Number(v).toFixed(2);
}

function debounce(fn, ms=300) {
    let t;
    return (...args)=>{
        clearTimeout(t);
        t = setTimeout(()=>fn(...args), ms);
    };
}

export { qs, qsa, showToast, initThemeToggle, formatMoney, debounce };