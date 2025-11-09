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
            data.items.forEach(i=>{
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
                data.content.forEach(p=>{
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
        // No endpoint listAll, reuse by user scanning (need an endpoint: /api/orders?mine) -> simplificamos simulando con last N via filter
        // Recomendación: crear endpoint en backend para listar órdenes del usuario. Aquí asumimos que existe /api/auth/me -> userId.
        try {
            const me = await API.get('/api/auth/me');
            // No endpoint directo: mostraremos una ayuda al usuario
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
    const ps = data.psRecords||[];
    return `
    <div class="panel">
      <h2>Orden #${order.id}</h2>
      <p><strong>Cliente:</strong> ${escapeHTML(order.customerName||'—')} | <strong>Email:</strong> ${escapeHTML(order.customerEmail||'—')}</p>
      <p><strong>Estado:</strong> ${escapeHTML(order.status)}</p>
      <h3>Items</h3>
      <table class="table">
        <thead><tr><th>ID</th><th>ProdID</th><th>Cant</th><th>$</th></tr></thead>
        <tbody>
          ${items.map(i=>`<tr><td>${i.id}</td><td>${i.productId}</td><td>${i.quantity}</td><td>${formatMoney(i.unitPrice*i.quantity)}</td></tr>`).join('')}
        </tbody>
      </table>
      <h3>Etapas</h3>
      <p><a class="btn small" href="order-warehouse.html?orderId=${order.id}">Almacén</a>
         <a class="btn small" href="order-packaging.html?orderId=${order.id}">Empaque</a>
         <a class="btn small" href="order-delivery.html?orderId=${order.id}">Entrega</a>
         <a class="btn small" href="order-payment.html?orderId=${order.id}">Pago</a>
         <a class="btn small" href="order-ps.html?orderId=${order.id}">Patrón P-S</a>
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
        <div class="generic-card" style="padding:.7rem;">
          <h4>Registros P-S</h4>
          ${ps.length? ps.map(r=>`<div>#${r.id} prod:${r.productId} ${escapeHTML(r.reason||'')} (${r.resolved?'RESUELTO':'PENDIENTE'})</div>`).join(''): '<em>Ninguno</em>'}
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

function initWarehousePage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    const form = qs('#warehouseForm');
    qs('#orderLink').href=`order-detail.html?orderId=${orderId}`;
    form.onsubmit = async e=>{
        e.preventDefault();
        const fd=new FormData(form);
        const body={
            inDate: fd.get('inDate')? new Date(fd.get('inDate')).toISOString(): null,
            outDate: fd.get('outDate')? new Date(fd.get('outDate')).toISOString(): null,
            stockChecked: fd.get('stockChecked')==='on',
            stockQty: fd.get('stockQty')? Number(fd.get('stockQty')): null,
            location: fd.get('location'),
            originCountry: fd.get('originCountry'),
            notes: fd.get('notes')
        };
        try {
            await API.put(`/api/orders/${orderId}/warehouse`, body);
            showToast('Almacén guardado','success');
            setTimeout(()=> location.href=`order-detail.html?orderId=${orderId}`,800);
        } catch {}
    };
}

function initPackagingPage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    qs('#orderLink').href=`order-detail.html?orderId=${orderId}`;
    qs('#packagingForm').onsubmit = async e=>{
        e.preventDefault();
        const fd=new FormData(e.target);
        const body={
            size: fd.get('size'),
            type: fd.get('type'),
            materials: fd.get('materials'),
            fragile: fd.get('fragile')==='on',
            notes: fd.get('notes')
        };
        try {
            await API.put(`/api/orders/${orderId}/packaging`, body);
            showToast('Empaque guardado','success');
            setTimeout(()=> location.href=`order-detail.html?orderId=${orderId}`,800);
        } catch {}
    };
}

function initDeliveryPage() {
    const params=new URLSearchParams(location.search);
    const orderId=params.get('orderId');
    qs('#orderLink').href=`order-detail.html?orderId=${orderId}`;
    qs('#deliveryForm').onsubmit= async e=>{
        e.preventDefault();
        const fd=new FormData(e.target);
        const body={
            method: fd.get('method'),
            address: fd.get('address'),
            scheduledDate: fd.get('scheduledDate')? new Date(fd.get('scheduledDate')).toISOString(): null,
            trackingCode: fd.get('trackingCode'),
            notes: fd.get('notes')
        };
        try {
            await API.put(`/api/orders/${orderId}/delivery`, body);
            showToast('Entrega guardada','success');
            setTimeout(()=> location.href=`order-detail.html?orderId=${orderId}`,800);
        } catch {}
    };
}

function initPaymentPage() {
    const params=new URLSearchParams(location.search);
    const orderId=params.get('orderId');
    qs('#orderLink').href=`order-detail.html?orderId=${orderId}`;
    qs('#paymentForm').onsubmit= async e=>{
        e.preventDefault();
        const fd=new FormData(e.target);
        const body={
            currency: fd.get('currency'),
            method: fd.get('method'),
            paidAt: fd.get('paidAt')? new Date(fd.get('paidAt')).toISOString(): null,
            notes: fd.get('notes')
        };
        try {
            await API.put(`/api/orders/${orderId}/payment`, body);
            showToast('Pago guardado','success');
            setTimeout(()=> location.href=`order-detail.html?orderId=${orderId}`,800);
        } catch {}
    };
}

function initPsPage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    const form = qs('#psForm');
    const list = qs('#psList');
    qs('#orderLink').href=`order-detail.html?orderId=${orderId}`;
    loadPs();

    async function loadPs() {
        list.innerHTML='Cargando...';
        try {
            const data = await API.get(`/api/orders/${orderId}`);
            const ps = data.psRecords||[];
            if (!ps.length) { list.innerHTML='<em>Sin registros</em>'; return; }
            list.innerHTML='';
            ps.forEach(r=>{
                const row = document.createElement('div');
                row.className='generic-card';
                row.style.padding='.5rem';
                row.innerHTML = `
          <strong>#${r.id}</strong> Prod:${r.productId} - ${escapeHTML(r.reason||'')} 
          <span class="badge small" style="background:${r.resolved?'var(--success)':'var(--warning)'}">
            ${r.resolved?'RESUELTO':'PENDIENTE'}
          </span>
          ${!r.resolved?`<button class="btn small success" data-resolve="${r.id}">Resolver</button>`:''}
        `;
                list.appendChild(row);
            });
            list.onclick = async e=>{
                if (e.target.matches('button[data-resolve]')) {
                    const id = e.target.getAttribute('data-resolve');
                    try {
                        await API.put(`/api/orders/ps/${id}/resolve`, {});
                        showToast('Registro P-S resuelto','success');
                        loadPs();
                    } catch {}
                }
            };
        } catch { list.innerHTML='Error'; }
    }

    form.onsubmit = async e=>{
        e.preventDefault();
        const fd = new FormData(form);
        const body = {
            productId: Number(fd.get('productId')),
            reason: fd.get('reason')
        };
        if (!body.productId || !body.reason) {
            showToast('productId y reason requeridos','error');
            return;
        }
        try {
            await API.post(`/api/orders/${orderId}/ps`, body);
            showToast('Registro P-S creado','success');
            form.reset();
            loadPs();
        } catch {}
    };
}

export {
    initOrderCreatePage, initOrderListPage, initOrderDetailPage,
    initWarehousePage, initPackagingPage, initDeliveryPage,
    initPaymentPage, initPsPage
};