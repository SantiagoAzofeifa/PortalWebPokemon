// orders.js
import API from './api.js';
import { qs, showToast, formatMoney, escapeHTML } from './util.js';

function initOrderCreatePage() {
    const productList = qs('#createOrderProducts');
    const form = qs('#orderCreateForm');
    loadCartPreview();
    loadProductsPreview();

    async function loadCartPreview() {
        if (!API.token()) return;
        try {
            const data = await API.get('/api/cart');
            const box = qs('#cartPreview');
            box.innerHTML = '';
            (data.items||[]).forEach(i=>{
                const div = document.createElement('div');
                div.className='generic-card';
                div.style.padding='.5rem .6rem';
                div.innerHTML = `<strong>ProdID ${i.productId}</strong> Cant: ${i.quantity} Total: ${formatMoney(i.unitPrice*i.quantity)}`;
                box.appendChild(div);
            });
        } catch {}
    }

    async function loadProductsPreview() {
        productList.innerHTML='Cargando...';
        try {
            const cats=['COMICS','FIGURAS','EVENTOS','SERIES'];
            productList.innerHTML='';
            for (const c of cats) {
                const data = await API.get(`/api/products?category=${c}&page=0&size=8`);
                (data.content||[]).forEach(p=>{
                    const cb = document.createElement('div');
                    cb.className='generic-card';
                    cb.style.padding='.5rem';
                    cb.innerHTML = `
            <label style="display:flex;align-items:center;gap:.4rem;">
              <input type="checkbox" name="productIds" value="${p.id}">
              <span>${escapeHTML(p.name)} (${formatMoney(p.price)})</span>
            </label>
          `;
                    productList.appendChild(cb);
                });
            }
        } catch {
            productList.innerHTML='Error cargando productos';
        }
    }

    form.onsubmit = async e=>{
        e.preventDefault();
        if (!API.token()) { showToast('Inicia sesión','error'); return; }
        const fd = new FormData(form);
        const order = {
            customerName: fd.get('customerName'),
            customerEmail: fd.get('customerEmail'),
            customerPhone: fd.get('customerPhone'),
            addressLine1: fd.get('addressLine1'),
            addressLine2: fd.get('addressLine2'),
            country: fd.get('country'),
            region: fd.get('region')
        };
        const productIds = fd.getAll('productIds').map(Number);
        if (!productIds.length) {
            showToast('Selecciona al menos un producto','error');
            return;
        }
        try {
            const r = await API.post('/api/orders',{ order, productIds });
            showToast('Orden creada','success');
            setTimeout(()=> location.href=`order-detail.html?orderId=${r.orderId}`,800);
        } catch {}
    };
}

function initOrderListPage() {
    const list = qs('#ordersList');
    if (!API.token()) { list.innerHTML='Inicia sesión.'; return; }
    loadOrders();

    async function loadOrders() {
        list.innerHTML = '<div class="generic-card" style="padding:1rem;">Cargando...</div>';
        try {
            const me = await API.get('/api/auth/me');
            list.innerHTML = `
        <div class="generic-card" style="padding:1rem;">
          <p>No hay endpoint para listar todas las órdenes en el backend actual. Puedes abrir una orden recién creada:</p>
          <p>Si acabas de crear una orden, usa su orderId en la URL: <code>order-detail.html?orderId=ID</code></p>
        </div>`;
            showToast('Considera agregar endpoint GET /api/orders?userId=...','warn',6000);
        } catch {
            list.innerHTML = '<div class="generic-card" style="padding:1rem;color:var(--danger)">Error</div>';
        }
    }
}

function initOrderDetailPage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    if (!orderId) {
        qs('#orderDetailContainer').innerHTML = 'Falta orderId';
        return;
    }
    loadDetail(orderId);
}

async function loadDetail(orderId) {
    const box = qs('#orderDetailContainer');
    box.innerHTML='Cargando...';
    try {
        const data = await API.get(`/api/orders/${orderId}`);
        box.innerHTML = renderDetail(data);
    } catch {
        box.innerHTML='Error';
    }
}

function renderDetail(data) {
    const order = data.order;
    const items = data.items||[];
    const wh = data.warehouse;
    const pk = data.packaging;
    const dv = data.delivery;
    const pm = data.payment;
    return `
    <div class="panel">
      <h2>Orden #${order.id}</h2>
      <p><strong>Cliente:</strong> ${escapeHTML(order.customerName||'—')} | <strong>Email:</strong> ${escapeHTML(order.customerEmail||'—')}</p>
      <p><strong>Estado:</strong> ${escapeHTML(order.status)}</p>
      <h3>Items</h3>
      <table class="table">
        <thead><tr><th>ID</th><th>ProdID</th><th>Categoría</th><th>Cant</th><th>$</th></tr></thead>
        <tbody>
          ${items.map(i=>`<tr><td>${i.id}</td><td>${i.productId}</td><td><span class="badge small">${escapeHTML(i.productCategory||'')}</span></td><td>${i.quantity}</td><td>${formatMoney(i.unitPrice*i.quantity)}</td></tr>`).join('')}
        </tbody>
      </table>
      <h3>Etapas</h3>
      <p>
         <a class="btn small" href="order-warehouse.html?orderId=${order.id}">Almacén</a>
         <a class="btn small" href="order-packaging.html?orderId=${order.id}">Empaque</a>
         <a class="btn small" href="order-delivery.html?orderId=${order.id}">Entrega</a>
         <a class="btn small" href="order-payment.html?orderId=${order.id}">Pago</a>
      </p>
      <div class="grid" style="grid-template-columns:repeat(auto-fit,minmax(250px,1fr));gap:1rem;">
        <div class="generic-card" style="padding:.7rem;">
          <h4>Almacén</h4>
          ${wh? renderWarehouse(wh): '<em>No registrado</em>'}
        </div>
        <div class="generic-card" style="padding:.7rem;">
          <h4>Empaque</h4>
          ${pk? renderPackaging(pk): '<em>No registrado</em>'}
        </div>
        <div class="generic-card" style="padding:.7rem;">
          <h4>Entrega</h4>
          ${dv? renderDelivery(dv): '<em>No registrado</em>'}
        </div>
        <div class="generic-card" style="padding:.7rem;">
          <h4>Pago</h4>
          ${pm? renderPayment(pm): '<em>No registrado</em>'}
        </div>
      </div>
    </div>
  `;
}

function renderWarehouse(w){
    return `
    <small>In: ${w.inDate||'—'} / Out: ${w.outDate||'—'}</small>
    <small>Stock OK: ${w.stockChecked}</small>
    <small>Cantidad: ${w.stockQty||'—'}</small>
    <small>Origen: ${escapeHTML(w.originCountry||'—')}</small>
    <small>Ubicación: ${escapeHTML(w.location||'—')}</small>
    <small>Notas: ${escapeHTML(w.notes||'—')}</small>
  `;
}
function renderPackaging(p){
    return `
    <small>Tamaño: ${escapeHTML(p.size||'—')}</small>
    <small>Tipo: ${escapeHTML(p.type||'—')}</small>
    <small>Materiales: ${escapeHTML(p.materials||'—')}</small>
    <small>Frágil: ${p.fragile}</small>
    <small>Notas: ${escapeHTML(p.notes||'—')}</small>
  `;
}
function renderDelivery(d){
    return `
    <small>Método: ${escapeHTML(d.method||'—')}</small>
    <small>Dirección: ${escapeHTML(d.address||'—')}</small>
    <small>Fecha programada: ${d.scheduledDate||'—'}</small>
    <small>Tracking: ${escapeHTML(d.trackingCode||'—')}</small>
    <small>Notas: ${escapeHTML(d.notes||'—')}</small>
  `;
}
function renderPayment(pm){
    return `
    <small>Moneda: ${escapeHTML(pm.currency||'—')}</small>
    <small>Items: ${pm.itemCount||0}</small>
    <small>Bruto: ${formatMoney(pm.grossAmount||0)}</small>
    <small>Neto: ${formatMoney(pm.netAmount||0)}</small>
    <small>Método: ${escapeHTML(pm.method||'—')}</small>
    <small>Fecha: ${pm.paidAt||'—'}</small>
  `;
}

export {
    initOrderCreatePage, initOrderListPage, initOrderDetailPage,
    renderWarehouse, renderPackaging, renderDelivery, renderPayment
};