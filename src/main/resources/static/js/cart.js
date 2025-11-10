import API from './api.js';
import { qs, formatMoney, showToast, escapeHTML } from './util.js';

function initCartPage() {
    qs('#clearCartBtn')?.addEventListener('click', async ()=>{
        if (!API.token()) { showToast('Inicia sesión','error'); return; }
        if (!confirm('¿Vaciar carrito?')) return;
        try { await API.del('/api/cart/clear'); showToast('Carrito vaciado','success'); loadCart(); } catch {}
    });
    loadCart();
}

async function loadCart() {
    const body = qs('#cartBody');
    const totalEl = qs('#cartTotal');
    const status = qs('#cartStatus');
    if (!API.token()) {
        status.textContent = 'Debes iniciar sesión.';
        body.innerHTML='';
        totalEl.textContent='Total: $0.00';
        return;
    }
    status.textContent = 'Cargando...';
    try {
        const data = await API.get('/api/cart');
        status.textContent = '';
        body.innerHTML = '';
        let sum = 0;
        (data.items||[]).forEach(it=>{
            const line = it.unitPrice * it.quantity;
            sum += line;
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td style="display:flex;gap:.6rem;align-items:center;">
          <img src="${it.image||'https://placehold.co/56x56?text=' + encodeURIComponent(it.productCategory||'?')}" alt="${escapeHTML(it.name||'')}" style="width:56px;height:56px;object-fit:contain;border-radius:6px;">
          <div>
            <div><strong>${escapeHTML((it.name||('#'+it.productId)))}</strong> <span class="badge small">${escapeHTML(it.productCategory||'')}</span></div>
            <small>ID: ${it.productId}</small>
          </div>
        </td>
        <td><input type="number" min="1" value="${it.quantity}" data-id="${it.id}" class="qty-input" style="width:70px;"></td>
        <td>${formatMoney(it.unitPrice)}</td>
        <td>${formatMoney(line)}</td>
        <td><button class="btn small danger outline" data-del="${it.id}">Eliminar</button></td>
      `;
            body.appendChild(tr);
        });
        totalEl.textContent = 'Total: ' + formatMoney(sum);
        body.onclick = async e=>{
            if (e.target.matches('button[data-del]')) {
                const id = e.target.getAttribute('data-del');
                try { await API.del(`/api/cart/items/${id}`); showToast('Item eliminado','success'); loadCart(); } catch {}
            }
        };
        body.onchange = async e=>{
            if (e.target.matches('.qty-input')) {
                const id = e.target.getAttribute('data-id');
                const qty = Number(e.target.value);
                try { await API.put(`/api/cart/items/${id}`, { quantity: qty }); showToast('Cantidad actualizada','success'); loadCart(); } catch {}
            }
        };
    } catch {
        status.textContent = 'Error cargando el carrito.';
    }
}

export { initCartPage };