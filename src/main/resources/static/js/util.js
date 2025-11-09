// util.js
function qs(sel, ctx=document){ return ctx.querySelector(sel); }
function qsa(sel, ctx=document){ return [...ctx.querySelectorAll(sel)]; }

function showToast(msg, type='info', timeout=4000) {
    const c = qs('#toastContainer'); if (!c) return;
    const d = document.createElement('div');
    d.className = `toast ${type==='error'?'error': type==='success'?'success': type==='warn'?'warn':''}`;
    d.setAttribute('role','alert');
    d.textContent = msg;
    c.appendChild(d);
    setTimeout(()=> d.remove(), timeout);
}

function formatMoney(v){ return '$' + Number(v).toFixed(2); }

function initTheme() {
    const btn = qs('#themeToggle');
    if (!btn) return;
    const saved = localStorage.getItem('theme');
    if (saved === 'dark') document.body.classList.add('theme-dark');
    btn.onclick = () => {
        document.body.classList.toggle('theme-dark');
        localStorage.setItem('theme', document.body.classList.contains('theme-dark')?'dark':'light');
    };
}

function debounce(fn, ms=300){
    let t; return (...args)=>{ clearTimeout(t); t=setTimeout(()=>fn(...args),ms); };
}

function escapeHTML(str='') {
    return str.replace(/[&<>"']/g, ch => ({
        '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
    }[ch]));
}

export { qs, qsa, showToast, formatMoney, initTheme, debounce, escapeHTML };