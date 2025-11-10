// orders.js
import API from './api.js';
import { qs, showToast, formatMoney, escapeHTML } from './util.js';
import { hydrateSessionUI } from './session.js';

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

async function initOrderListPage() {
    const list = qs('#ordersList');
    if (!list) return;

    if (!API.token()) {
        list.innerHTML = `<div class="generic-card" style="padding:1rem;">Inicia sesión para ver tus órdenes.</div>`;
        return;
    }

    list.innerHTML = `<div class="generic-card" style="padding:1rem;">Cargando órdenes...</div>`;

    try {
        const orders = await API.get('/api/orders/mine');

        if (!orders || orders.length === 0) {
            list.innerHTML = `
        <div class="generic-card" style="padding:1rem;">
          <p>No tienes órdenes registradas aún.</p>
        </div>`;
            return;
        }

        // Renderizar lista
        list.innerHTML = orders.map(o => `
      <div class="generic-card order-card">
        <div class="order-info">
          <strong>Orden #${o.id}</strong><br>
          Estado: <span class="badge ${o.status.toLowerCase()}">${o.status}</span><br>
          Creada: ${formatLocalDateTime(o.createdAt)}<br>
          Cliente: ${o.customerName || '—'}
        </div>
        <div class="order-actions">
          <button class="btn primary" data-id="${o.id}">Ver detalle</button>
        </div>
      </div>
    `).join('');

        // Asignar evento a los botones
        list.querySelectorAll('button[data-id]').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                location.href = `order-detail.html?orderId=${id}`;
            });
        });

    } catch (err) {
        console.error('[initOrderListPage] Error al cargar órdenes', err);
        list.innerHTML = `
      <div class="generic-card" style="padding:1rem;color:var(--danger)">
        Error al cargar tus órdenes.
      </div>`;
        showToast('No se pudieron cargar las órdenes', 'error');
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
        hydrateSessionUI();
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
      <p data-requires-role="ADMIN">
         <a class="btn small" href="order-warehouse.html?orderId=${order.id}" data-requires-role="ADMIN">Almacén</a>
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

// Convierte una fecha ISO (UTC) a formato local legible en Costa Rica
function formatLocalDateTime(isoString) {
    if (!isoString) return '—';
    const date = new Date(isoString);
    return date.toLocaleString('es-CR', {
        dateStyle: 'short',
        timeStyle: 'short',
        hour12: false
    });
}

function renderWarehouse(w){
    return `
    <small>In: ${formatLocalDateTime(w.inDate)} / Out: ${formatLocalDateTime(w.outDate)}</small>
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
    <small>Fecha programada: ${formatLocalDateTime(d.scheduledDate)}</small>
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
    <small>Fecha: ${formatLocalDateTime(pm.paidAt)}</small>
  `;
}


async function initWarehousePage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    if (!orderId) {
        alert('Falta orderId en la URL');
        return;
    }

    const productList = document.getElementById('productList');
    if (!productList) return;

    productList.innerHTML = '<p>Cargando productos...</p>';

    try {
        const data = await API.get(`/api/orders/${orderId}`);

        if (!data.items || data.items.length === 0) {
            productList.innerHTML = '<p>No hay productos para este pedido.</p>';
            return;
        }

        // Renderizar lista de productos con checkbox
        const html = data.items.map(item => `
            <label class="form-field checkbox-field">
                <input type="checkbox" name="productCheck" value="${item.productId}">
                <strong>ID:</strong> ${item.productId} — 
                <span>Categoría:</span> ${item.productCategory} — 
                <span>Cantidad:</span> ${item.quantity}
            </label>
        `).join('');

        productList.innerHTML = html;

        // Si ya existen datos de warehouse, cargarlos en el formulario
        const form = document.getElementById('warehouseForm');
        if (!form) return;

        if (data.warehouse) {
            const w = data.warehouse;

            // Convertir fechas a hora local (Costa Rica, sin UTC)
            form.inDate.value = w.inDate ? localDateTimeValue(w.inDate) : '';
            form.outDate.value = w.outDate ? localDateTimeValue(w.outDate) : '';
            form.stockQty.value = w.stockQty ?? '';
            form.location.value = w.location ?? '';
            form.originCountry.value = w.originCountry ?? '';
            form.notes.value = w.notes ?? '';

            // Si hay productos marcados como revisados (stockChecked)
            if (w.stockChecked) {
                const checkboxes = document.querySelectorAll('input[name="productCheck"]');
                checkboxes.forEach(cb => cb.checked = true);
            }
        }

        // Registrar submit
        await handleWarehouseSubmit(orderId, form);

        // Enlace "Volver Detalle"
        const orderLink = document.getElementById('orderLink');
        if (orderLink) {
            orderLink.addEventListener('click', (e) => {
                e.preventDefault();
                location.href = `order-detail.html?orderId=${orderId}`;
            });
        }

    } catch (err) {
        console.error(err);
        productList.innerHTML = '<p>Error al cargar los productos.</p>';
        showToast('Error al cargar datos del almacén', 'error');
    }
}

/**
 * Convierte un valor ISO a formato "datetime-local" en hora local (Costa Rica).
 * Evita el desfase de UTC al usar toISOString().
 */
function localDateTimeValue(isoString) {
    const date = new Date(isoString);
    // Ajusta al huso horario local
    const tzOffset = date.getTimezoneOffset() * 60000; // minutos → ms
    const local = new Date(date.getTime() - tzOffset);
    return local.toISOString().slice(0, 16);
}

async function handleWarehouseSubmit(orderId, form) {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const allCheckboxes = Array.from(document.querySelectorAll('input[name="productCheck"]'));
        const checkedBoxes = allCheckboxes.filter(cb => cb.checked);
        const uncheckedBoxes = allCheckboxes.filter(cb => !cb.checked);

        const allChecked = checkedBoxes.length === allCheckboxes.length && allCheckboxes.length > 0;

        // Convertir fechas a formato ISO UTC (para enviar al backend)
        const toIsoString = (value) => value ? new Date(value).toISOString() : null;

        const payload = {
            orderId,
            inDate: toIsoString(formData.get('inDate')),
            outDate: toIsoString(formData.get('outDate')),
            stockChecked: allChecked,
            stockQty: parseInt(formData.get('stockQty') || 0),
            location: formData.get('location'),
            originCountry: formData.get('originCountry'),
            notes: formData.get('notes')
        };

        try {
            await API.put(`/api/orders/${orderId}/warehouse`, payload);
            showToast('Datos de almacén guardados correctamente', 'success');

            // Si hay productos que no pasaron el check, crear alerta
            if (uncheckedBoxes.length > 0) {
                const failedProducts = uncheckedBoxes.map(cb => cb.value).join(',');
                await API.post(`/api/orders/${orderId}?productos=${failedProducts}`);
            }

        } catch (err) {
            console.error('[Warehouse error]', err);
            showToast('Error al guardar datos del almacén', 'error');
        }
    });
}

/*
async function initWarehousePage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    if (!orderId) {
        alert('Falta orderId en la URL');
        return;
    }

    const productList = document.getElementById('productList');
    if (!productList) return;

    productList.innerHTML = '<p>Cargando productos...</p>';

    try {
        const data = await API.get(`/api/orders/${orderId}`);

        if (!data.items || data.items.length === 0) {
            productList.innerHTML = '<p>No hay productos para este pedido.</p>';
            return;
        }

        // Renderizar lista de productos con checkbox
        const html = data.items.map(item => `
    <label class="form-field checkbox-field">
        <input type="checkbox" name="productCheck" value="${item.productId}">
        <strong>ID:</strong> ${item.productId} — 
        <span>Categoría:</span> ${item.productCategory} — 
        <span>Cantidad:</span> ${item.quantity}
    </label>
        `).join('');

        productList.innerHTML = html;

        const form = document.getElementById('warehouseForm');
        if (form) await handleWarehouseSubmit(orderId, form);
    } catch (err) {
        console.error(err);
        productList.innerHTML = '<p>Error al cargar los productos.</p>';
    }
}

function toIsoString(value) {
    if (!value) return null;
    // Convierte "2025-11-09T21:08" a "2025-11-09T21:08:00Z"
    const date = new Date(value);
    return date.toISOString();
}

async function handleWarehouseSubmit(orderId, form) {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const allCheckboxes = Array.from(document.querySelectorAll('input[name="productCheck"]'));
        const checkedBoxes = allCheckboxes.filter(cb => cb.checked);
        const uncheckedBoxes = allCheckboxes.filter(cb => !cb.checked);

        const allChecked = checkedBoxes.length === allCheckboxes.length && allCheckboxes.length > 0;

        const toIsoString = (value) => value ? new Date(value).toISOString() : null;

        const payload = {
            orderId,
            inDate: toIsoString(formData.get('inDate')),
            outDate: toIsoString(formData.get('outDate')),
            stockChecked: allChecked,
            stockQty: parseInt(formData.get('stockQty') || 0),
            location: formData.get('location'),
            originCountry: formData.get('originCountry'),
            notes: formData.get('notes')
        };

        try {
            // Guardar datos del almacen
            await API.put(`/api/orders/${orderId}/warehouse`, payload);
            showToast('Datos de almacén guardados', 'success');

            // Si hay productos que no pasaron el check, crear alerta
            if (uncheckedBoxes.length > 0) {
                const failedProducts = uncheckedBoxes.map(cb => cb.value).join(',');

                await API.post(`/api/orders/${orderId}?productos=${failedProducts}`);
            }

        } catch (err) {
            console.error(err);
            showToast('Error al guardar', 'error');
        }
    });
}

*/

//funciones para packagin
export async function initPackagingPage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    if (!orderId) {
        alert('Falta orderId en la URL');
        return;
    }

    const form = document.getElementById('packagingForm');
    if (!form) return;

    // Cargar datos existentes si hay
    try {
        const data = await API.get(`/api/orders/${orderId}`);

        if (data.packaging) {
            const p = data.packaging;
            form.size.value = p.size || '';
            form.type.value = p.type || '';
            form.materials.value = p.materials || '';
            form.fragile.checked = !!p.fragile;
            form.notes.value = p.notes || '';
        }
    } catch (err) {
        console.error('Error al cargar datos de empaque:', err);
    }

    // Inicializar envío del formulario
    await handlePackagingSubmit(orderId, form);
}

async function handlePackagingSubmit(orderId, form) {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(form);

        const payload = {
            orderId,
            size: formData.get('size'),
            type: formData.get('type'),
            materials: formData.get('materials'),
            fragile: formData.get('fragile') !== null, // checkbox
            notes: formData.get('notes')
        };

        try {
            await API.put(`/api/orders/${orderId}/packaging`, payload);
            showToast('Datos de empaque guardados', 'success');
        } catch (err) {
            console.error('Error al guardar empaque:', err);
            showToast('Error al guardar empaque', 'error');
        }
    });
}

//Order (no uber eats jaja)
export async function initDeliveryPage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    if (!orderId) {
        alert('Falta orderId en la URL');
        return;
    }

    const form = document.getElementById('deliveryForm');
    if (!form) return;

    // Cargar datos de la orden completa
    try {
        const data = await API.get(`/api/orders/${orderId}`);

        // Validar si existe bloque "delivery"
        if (data.delivery) {
            const d = data.delivery;
            form.method.value = d.method || '';
            form.address.value = d.address || '';
            form.scheduledDate.value = d.scheduledDate
            form.scheduledDate.value = d.scheduledDate
                ? localDateTimeValue(d.scheduledDate)
                : '';
            form.trackingCode.value = d.trackingCode || '';
            form.notes.value = d.notes || '';
        }
    } catch (err) {
        console.warn('No se pudieron cargar los datos de entrega:', err);
    }

    // Configurar link de regreso al detalle del pedido
    const orderLink = document.getElementById('orderLink');
    if (orderLink) {
        orderLink.href = `order-detail.html?orderId=${orderId}`;
    }

    // Asociar el manejador del submit
    await handleDeliverySubmit(orderId, form);
}

export async function handleDeliverySubmit(orderId, form) {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const payload = {
            method: formData.get('method'),
            address: formData.get('address'),
            scheduledDate: formData.get('scheduledDate')
                ? new Date(formData.get('scheduledDate')).toISOString()
                : null,
            trackingCode: formData.get('trackingCode'),
            notes: formData.get('notes')
        };

        try {
            await API.put(`/api/orders/${orderId}/delivery`, payload);
            showToast('Datos de entrega guardados correctamente', 'success');
        } catch (err) {
            console.error(err);
            showToast('Error al guardar los datos de entrega', 'error');
        }
    });
}

//Pagina de pago
export async function initPaymentPage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    if (!orderId) {
        alert('Falta orderId en la URL');
        return;
    }

    const form = document.getElementById('paymentForm');
    if (!form) return;

    // Intentar cargar datos previos del pago
    try {
        const data = await API.get(`/api/orders/${orderId}`);

        if (data.payment) {
            const p = data.payment;

            form.currency.value = p.currency || 'USD';
            form.method.value = p.method || '';
            form.paidAt.value = p.paidAt ? localDateTimeValue(p.paidAt) : '';
            form.notes.value = p.notes || '';
        }

    } catch (err) {
        console.error('[initPaymentPage] Error al cargar datos:', err);
        showToast('Error al cargar datos de pago', 'error');
    }

    // Registrar manejador del submit
    await handlePaymentSubmit(orderId, form);

    const orderLink = document.getElementById('orderLink');
    if (orderLink) {
        orderLink.addEventListener('click', (e) => {
            e.preventDefault();
            location.href = `order-detail.html?orderId=${orderId}`;
        });
    }
}

async function handlePaymentSubmit(orderId, form) {
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const formData = new FormData(form);
        const payload = {
            orderId,
            currency: formData.get('currency'),
            method: formData.get('method'),
            paidAt: formData.get('paidAt')
                ? new Date(formData.get('paidAt')).toISOString()
                : null,
            notes: formData.get('notes'),
        };

        try {
            const response = await API.put(`/api/orders/${orderId}/payment`, payload);
            console.log('[Payment saved]', response);
            showToast('Pago guardado correctamente', 'success');
        } catch (err) {
            console.error('[Payment error]', err);
            showToast('Error al guardar el pago', 'error');
        }
    });
}

//Funcion para ver las ordenes de todas las personas
async function initAllOrderListPage() {
    const list = qs('#ordersList');
    if (!list) return;

    if (!API.token()) {
        list.innerHTML = `<div class="generic-card" style="padding:1rem;">Fallo al cargar.</div>`;
        return;
    }

    list.innerHTML = `<div class="generic-card" style="padding:1rem;">Cargando órdenes...</div>`;

    try {
        const orders = await API.get('/api/orders/all');

        if (!orders || orders.length === 0) {
            list.innerHTML = `
        <div class="generic-card" style="padding:1rem;">
          <p>No tienes órdenes registradas aún.</p>
        </div>`;
            return;
        }

        // Renderizar lista
        list.innerHTML = orders.map(o => `
      <div class="generic-card order-card">
        <div class="order-info">
          <strong>Orden #${o.id}</strong><br>
          Estado: <span class="badge ${o.status.toLowerCase()}">${o.status}</span><br>
          Creada: ${formatLocalDateTime(o.createdAt)}<br>
          Cliente: ${o.customerName || '—'}
        </div>
        <div class="order-actions">
          <button class="btn primary" data-id="${o.id}">Ver detalle</button>
        </div>
      </div>
    `).join('');

        // Asignar evento a los botones
        list.querySelectorAll('button[data-id]').forEach(btn => {
            btn.addEventListener('click', () => {
                const id = btn.getAttribute('data-id');
                location.href = `order-detail.html?orderId=${id}`;
            });
        });

    } catch (err) {
        console.error('[initOrderListPage] Error al cargar órdenes', err);
        list.innerHTML = `
      <div class="generic-card" style="padding:1rem;color:var(--danger)">
        Error al cargar tus órdenes.
      </div>`;
        showToast('No se pudieron cargar las órdenes', 'error');
    }
}




export {
    initOrderCreatePage, initOrderListPage, initOrderDetailPage,
    renderWarehouse, renderPackaging, renderDelivery, renderPayment, initWarehousePage,
    handleWarehouseSubmit, initAllOrderListPage
};