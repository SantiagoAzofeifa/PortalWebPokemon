import API from './api.js';
import { qs, formatMoney, showToast } from './util.js';

function initCartPage() {
    loadCart();
    qs('#clearCartBtn').onclick = async () => {
        try {
            await API.del('/api/cart/clear');
            showToast('Carrito vaciado','success');
            loadCart();
        } catch {}
    };
}

async function loadCart() {
    const body = qs('#cartBody');
    const totalEl = qs('#cartTotal');
    const status = qs('#cartStatus');
    body.innerHTML = '';
    totalEl.textContent = 'Total: $0.00';
    if (!API.token()) {
        status.textContent = 'Debes iniciar sesión para ver tu carrito.';
        return;
    }
    status.textContent = 'Cargando...';
    try {
        const data = await API.get('/api/cart');
        status.textContent = '';
        let sum = 0;
        data.items.forEach(it=>{
            const tr = document.createElement('tr');
            const lineTotal = it.unitPrice * it.quantity;
            sum += lineTotal;
            tr.innerHTML = `
        <td>${it.productId}</td>
        <td><input type="number" min="1" value="${it.quantity}" data-id="${it.id}" class="qty-input"></td>
        <td>${formatMoney(it.unitPrice)}</td>
        <td>${formatMoney(lineTotal)}</td>
        <td>
          <button class="btn small danger outline" data-del="${it.id}">Eliminar</button>
        </td>
      `;
            body.appendChild(tr);
        });
        totalEl.textContent = 'Total: ' + formatMoney(sum);
        body.addEventListener('change', onQtyChange);
        body.addEventListener('click', onDelete);
    } catch {
        status.textContent = 'Error cargando el carrito.';
    }
}

async function onQtyChange(e) {
    if (!e.target.matches('.qty-input')) return;
    const id = e.target.getAttribute('data-id');
    const qty = parseInt(e.target.value);
    try {
        await API.put(`/api/cart/items/${id}`, { quantity: qty });
        showToast('Cantidad actualizada','success');
        loadCart();
    } catch {}
}

async function onDelete(e) {
    if (!e.target.matches('button[data-del]')) return;
    const id = e.target.getAttribute('data-del');
    try {
        await API.del(`/api/cart/items/${id}`);
        showToast('Item eliminado','success');
        loadCart();
    } catch {}
}

export { initCartPage };