// admin-extended.js (con gestión de órdenes)
import API from './api.js';
import { qs, escapeHTML, showToast, formatMoney } from './util.js';
import { bindModalTriggers, closeModal } from './modal.js';

bindModalTriggers();
initAdminExtended();

let currentOrderId = null;

async function initAdminExtended() {
    if (!(await verifyAdmin())) return;
    await populateAllowedCountries();
    loadDashboard();
    bindSessionTimeout();
    setupUsers();
    setupRules();
    setupInternalProducts();
    setupGlobalOrders(); // NUEVO
}

/* ---------- Verificación ADMIN ---------- */
async function verifyAdmin() {
    try {
        const me = await API.get('/api/auth/me');
        if (me.role !== 'ADMIN') {
            showToast('Acceso sólo ADMIN','error');
            setTimeout(()=> location.href='index.html',1500);
            return false;
        }
        return true;
    } catch {
        showToast('Debes iniciar sesión','error');
        location.href='login.html';
        return false;
    }
}

/* ---------- Países permitidos ---------- */
async function populateAllowedCountries(){
    const sel = qs('#prodCountry');
    if (!sel) return;
    try {
        const list = await API.get('/api/catalog/allowed-countries');
        sel.innerHTML = list.map(c=>`<option value="${c}">${c}</option>`).join('');
    } catch {
        const fallback = ['CR','US','MX','ES','AR','CL','BR','FR','DE','JP','CN'];
        sel.innerHTML = fallback.map(c=>`<option value="${c}">${c}</option>`).join('');
    }
}

/* ---------- Dashboard ---------- */
async function loadDashboard() {
    const timeoutEl = qs('#dashTimeout');
    const usersEl = qs('#dashUsers');
    const ordersEl = qs('#dashOrders');

    if (timeoutEl) {
        try { timeoutEl.textContent = (await API.get('/api/admin/session-timeout')).timeoutSeconds; }
        catch { timeoutEl.textContent='Err'; }
    }
    if (usersEl) {
        try { usersEl.textContent = (await API.get('/api/admin/users')).length; }
        catch { usersEl.textContent='Err'; }
    }
    if (ordersEl) {
        try { ordersEl.textContent = (await API.get('/api/orders/mine')).length; }
        catch { ordersEl.textContent='Err'; }
    }
}

/* ---------- Timeout Sesión ---------- */
function bindSessionTimeout() {
    const form = qs('#sessionTimeoutForm');
    if (!form) return;
    form.onsubmit = async e=>{
        e.preventDefault();
        const value = Number(qs('#newTimeout').value);
        try {
            const d = await API.put('/api/admin/session-timeout', { timeoutSeconds: value });
            showToast('Timeout actualizado','success');
            const timeoutEl = qs('#dashTimeout');
            if (timeoutEl) timeoutEl.textContent = d.timeoutSeconds;
        } catch {}
    };
}

/* ---------- Usuarios ---------- */
function setupUsers() {
    const body = qs('#userBody');
    const filterForm = qs('#userFilterForm');
    const clearBtn = qs('#userClear');
    if (!body) return;

    filterForm && (filterForm.onsubmit = e => { e.preventDefault(); loadUsers(); });
    clearBtn && (clearBtn.onclick = ()=> { qs('#userQ').value=''; qs('#userRole').value=''; loadUsers(); });
    loadUsers();

    async function loadUsers() {
        body.innerHTML='<tr><td colspan="5">Cargando...</td></tr>';
        try {
            const users = await API.get('/api/admin/users');
            const q = (qs('#userQ')?.value||'').trim().toLowerCase();
            const role = qs('#userRole')?.value.trim();
            const filtered = users.filter(u=>{
                let ok=true;
                if (q && !u.username.toLowerCase().includes(q)) ok=false;
                if (role && u.role!==role) ok=false;
                return ok;
            });
            body.innerHTML='';
            filtered.forEach(u=>{
                const tr=document.createElement('tr');
                tr.innerHTML = `
          <td>${u.id}</td>
          <td>${escapeHTML(u.username)}</td>
          <td><span class="badge ${u.role==='ADMIN'?'accent':'success'}">${u.role}</span></td>
          <td>${u.active?'<span class="badge success">Activo</span>':'<span class="badge danger">Inactivo</span>'}</td>
          <td>
            <button class="btn small" data-rol="${u.id}">Rol</button>
            <button class="btn small outline" data-act="${u.id}">Activo</button>
            <button class="btn small danger outline" data-del="${u.id}">Eliminar</button>
          </td>`;
                body.appendChild(tr);
            });
            body.onclick = async e=>{
                if (e.target.matches('[data-rol]')) {
                    const id=e.target.getAttribute('data-rol');
                    const user=filtered.find(x=>x.id==id);
                    const newRole = user.role==='ADMIN'?'USER':'ADMIN';
                    try { await API.put(`/api/admin/users/${id}`, { role:newRole }); showToast('Rol cambiado','success'); loadUsers(); } catch {}
                }
                if (e.target.matches('[data-act]')) {
                    const id=e.target.getAttribute('data-act');
                    const user=filtered.find(x=>x.id==id);
                    try { await API.put(`/api/admin/users/${id}`, { active: !user.active }); showToast('Activo cambiado','success'); loadUsers(); } catch {}
                }
                if (e.target.matches('[data-del]')) {
                    const id=e.target.getAttribute('data-del');
                    if (!confirm('¿Eliminar usuario?')) return;
                    try { await API.del(`/api/admin/users/${id}`); showToast('Usuario eliminado','success'); loadUsers(); } catch {}
                }
            };
        } catch {
            body.innerHTML='<tr><td colspan="5" style="color:var(--danger)">Error</td></tr>';
        }
    }

    // Modal nuevo usuario
    const userNewForm = qs('#userNewForm');
    userNewForm?.addEventListener('submit', async e=>{
        e.preventDefault();
        const fd=new FormData(userNewForm);
        const body=Object.fromEntries(fd.entries());
        try {
            await API.post('/api/auth/register', body);
            showToast('Usuario creado','success');
            userNewForm.reset();
            closeModal('modalUserNew');
            loadUsers();
        } catch {}
    });
}

/* ---------- Órdenes Globales y Detalle ---------- */
function setupGlobalOrders() {
    const tbody = qs('#ordersGlobalBody');
    const form = qs('#orderFilterForm');
    const clearBtn = qs('#ofClear');

    if (!tbody) return;

    form && (form.onsubmit = e=> { e.preventDefault(); loadGlobalOrders(); });
    clearBtn && (clearBtn.onclick = ()=>{
        qs('#ofUserId').value=''; qs('#ofStatus').value='';
        qs('#ofFrom').value=''; qs('#ofTo').value='';
        loadGlobalOrders();
    });

    tbody.onclick = e=>{
        if (e.target.matches('[data-view-order]')) {
            const id = Number(e.target.getAttribute('data-view-order'));
            loadOrderDetail(id);
        }
        if (e.target.matches('[data-del-order]')) {
            const id = Number(e.target.getAttribute('data-del-order'));
            if (!confirm('¿Eliminar orden?')) return;
            deleteOrder(id);
        }
    };

    loadGlobalOrders();

    async function loadGlobalOrders() {
        tbody.innerHTML='<tr><td colspan="5">Cargando...</td></tr>';
        try {
            const orders = await API.get('/api/orders/all');
            // Filtros básicos
            const fUserId = qs('#ofUserId').value ? Number(qs('#ofUserId').value) : null;
            const fStatus  = qs('#ofStatus').value.trim().toUpperCase();
            const fFrom = qs('#ofFrom').value? new Date(qs('#ofFrom').value).getTime(): null;
            const fTo   = qs('#ofTo').value? new Date(qs('#ofTo').value).getTime(): null;

            const filtered = orders.filter(o=>{
                let ok=true;
                if (fUserId && o.userId!==fUserId) ok=false;
                if (fStatus && o.status.toUpperCase()!==fStatus) ok=false;
                if (fFrom && new Date(o.createdAt).getTime()<fFrom) ok=false;
                if (fTo && new Date(o.createdAt).getTime()>fTo) ok=false;
                return ok;
            });

            tbody.innerHTML='';
            if (!filtered.length) {
                tbody.innerHTML='<tr><td colspan="5">Sin resultados</td></tr>';
                return;
            }
            filtered.forEach(o=>{
                const tr = document.createElement('tr');
                tr.innerHTML = `
          <td>${o.id}</td>
          <td>${o.userId}</td>
          <td><span class="badge">${escapeHTML(o.status)}</span></td>
          <td>${o.createdAt||''}</td>
          <td>
            <button class="btn small" data-view-order="${o.id}">Ver</button>
            <button class="btn small danger outline" data-del-order="${o.id}">Eliminar</button>
          </td>
        `;
                tbody.appendChild(tr);
            });
        } catch {
            tbody.innerHTML='<tr><td colspan="5" style="color:var(--danger)">Error cargando</td></tr>';
        }
    }
}

async function deleteOrder(id) {
    try {
        await API.del(`/api/orders/${id}`);
        showToast('Orden eliminada','success');
        // Reset detalle si era la que estaba viendo
        if (currentOrderId === id) {
            currentOrderId = null;
            qs('#orderDetailWrapper').style.display='none';
            qs('#orderItemsWrapper').style.display='none';
            qs('#orderStagesWrapper').style.display='none';
            qs('#selectedOrderInfo').textContent='Orden eliminada. Selecciona otra.';
        }
        setupGlobalOrders(); // recargar lista
    } catch {
        showToast('Error eliminando orden','error');
    }
}

async function loadOrderDetail(orderId) {
    const info = qs('#selectedOrderInfo');
    const detailWrap = qs('#orderDetailWrapper');
    const itemsWrap = qs('#orderItemsWrapper');
    const stagesWrap = qs('#orderStagesWrapper');
    const itemsBody = qs('#orderItemsBody');
    const deleteBtn = qs('#deleteOrderBtn');
    currentOrderId = orderId;

    info.textContent = `Cargando detalle de orden #${orderId}...`;
    detailWrap.style.display='none';
    itemsWrap.style.display='none';
    stagesWrap.style.display='none';

    try {
        const data = await API.get(`/api/orders/${orderId}`);
        const o = data.order;
        const items = data.items||[];
        const warehouse = data.warehouse;
        const packaging = data.packaging;
        const delivery = data.delivery;
        const payment = data.payment;

        // Info básica de la orden
        detailWrap.innerHTML = `
      <div class="generic-card" style="padding:.7rem;">
        <strong>Orden #${o.id}</strong><br>
        Usuario: ${o.userId}<br>
        Cliente: ${escapeHTML(o.customerName||'—')}<br>
        Email: ${escapeHTML(o.customerEmail||'—')}<br>
        País Destino: ${escapeHTML(o.country||'—')}<br>
        Estado: <span class="badge">${escapeHTML(o.status)}</span><br>
        Creada: ${o.createdAt||'—'}
      </div>
    `;
        detailWrap.style.display='grid';

        // Items
        itemsBody.innerHTML='';
        items.forEach(i=>{
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>${i.id}</td>
        <td>${i.productId}</td>
        <td><span class="badge small">${escapeHTML(i.productCategory||'')}</span></td>
        <td>${i.quantity}</td>
        <td>${formatMoney(i.unitPrice*i.quantity)}</td>
      `;
            itemsBody.appendChild(tr);
        });
        itemsWrap.style.display='block';

        // Etapas vistas
        renderStageView('#warehouseView', warehouse, [
            'inDate','outDate','stockChecked','stockQty','location','originCountry','notes'
        ]);
        renderStageView('#packagingView', packaging, [
            'size','type','materials','fragile','notes'
        ]);
        renderStageView('#deliveryView', delivery, [
            'method','address','scheduledDate','trackingCode','notes'
        ]);
        renderStageView('#paymentView', payment, [
            'currency','itemCount','grossAmount','netAmount','method','paidAt','notes'
        ]);
        stagesWrap.style.display='block';

        // Bind forms
        bindStageForms(orderId);

        deleteBtn.onclick = ()=> {
            if (!confirm('¿Eliminar orden completa?')) return;
            deleteOrder(orderId);
        };

        info.textContent = `Detalle de orden #${orderId}`;
    } catch {
        info.textContent = 'Error cargando detalle.';
    }
}

function renderStageView(selector, obj, fields) {
    const el = qs(selector);
    if (!el) return;
    if (!obj) {
        el.innerHTML='<em>Sin datos</em>';
        return;
    }
    el.innerHTML = fields.map(f=> `<small><strong>${f}:</strong> ${escapeHTML(String(obj[f]??'—'))}</small>`).join('<br>');
}

function toIso(val) {
    if (!val) return null;
    // val se interpreta como hora local; toISOString lo emite en UTC con 'Z'
    const d = new Date(val);
    return new Date(d.getTime() - d.getTimezoneOffset() * 60000).toISOString();
}

function bindStageForms(orderId) {
    // Warehouse
    const wForm = qs('#warehouseForm');
    wForm && (wForm.onsubmit = async e=>{
        e.preventDefault();
        const fd = new FormData(wForm);
        const body = Object.fromEntries(fd.entries());
        body.stockChecked = body.stockChecked === 'true';
        body.stockQty = body.stockQty ? Number(body.stockQty) : null;
        body.inDate = toIso(fd.get('inDate'));
        body.outDate = toIso(fd.get('outDate'));
        try {
            await API.put(`/api/orders/${orderId}/warehouse`, body);
            showToast('Warehouse guardado','success');
            loadOrderDetail(orderId);
        } catch (err) {
            showToast(err?.message || 'Error guardando warehouse','error');
        }
    });

    // Packaging (sin fechas)
    const pForm = qs('#packagingForm');
    pForm && (pForm.onsubmit = async e=>{
        e.preventDefault();
        const fd = new FormData(pForm);
        const body = Object.fromEntries(fd.entries());
        body.fragile = body.fragile === 'true';
        try {
            await API.put(`/api/orders/${orderId}/packaging`, body);
            showToast('Packaging guardado','success');
            loadOrderDetail(orderId);
        } catch (err) {
            showToast(err?.message || 'Error guardando packaging','error');
        }
    });

    // Delivery
    const dForm = qs('#deliveryForm');
    dForm && (dForm.onsubmit = async e=>{
        e.preventDefault();
        const fd = new FormData(dForm);
        const body = Object.fromEntries(fd.entries());
        body.scheduledDate = toIso(fd.get('scheduledDate'));
        try {
            await API.put(`/api/orders/${orderId}/delivery`, body);
            showToast('Delivery guardado','success');
            loadOrderDetail(orderId);
        } catch (err) {
            showToast(err?.message || 'Error guardando delivery','error');
        }
    });

    // Payment
    const payForm = qs('#paymentForm');
    payForm && (payForm.onsubmit = async e=>{
        e.preventDefault();
        const fd = new FormData(payForm);
        const body = Object.fromEntries(fd.entries());
        body.paidAt = toIso(fd.get('paidAt'));
        try {
            await API.put(`/api/orders/${orderId}/payment`, body);
            showToast('Payment guardado','success');
            loadOrderDetail(orderId);
        } catch (err) {
            showToast(err?.message || 'Error guardando payment','error');
        }
    });
}

/* ---------- Reglas Pokémon ---------- */
function setupRules() {
    const form = qs('#rulesForm');
    if (!form) return;
    const loadBtn = qs('#ruleLoadBtn');
    const delBtn = qs('#ruleDeleteBtn');
    const status = qs('#ruleStatus');

    form.onsubmit = async e=>{
        e.preventDefault();
        const pid=Number(qs('#rulePokemonId').value);
        if (!pid) { showToast('pokemonId requerido','error'); return; }
        const body={
            originCountry: qs('#ruleOrigin').value,
            availableCountriesCsv: qs('#ruleAvail').value,
            bannedCountriesCsv: qs('#ruleBanned').value,
            notes: qs('#ruleNotes').value
        };
        try {
            const saved = await API.put(`/api/admin/rules/${pid}`, body);
            showToast('Regla guardada','success');
            status.textContent='Guardada ID interno: '+saved.id;
        } catch {}
    };

    loadBtn && (loadBtn.onclick = async ()=>{
        const pid=Number(qs('#rulePokemonId').value);
        if (!pid) { showToast('pokemonId requerido','error'); return; }
        try {
            const rule = await API.get(`/api/admin/rules/${pid}`);
            if (!rule){ showToast('No existe regla','warn'); return; }
            qs('#ruleOrigin').value = rule.originCountry||'';
            qs('#ruleAvail').value = rule.availableCountriesCsv||'';
            qs('#ruleBanned').value = rule.bannedCountriesCsv||'';
            qs('#ruleNotes').value = rule.notes||'';
            showToast('Regla cargada','success');
            status.textContent='Regla cargada';
        } catch {}
    });

    delBtn && (delBtn.onclick = async ()=>{
        const pid=Number(qs('#rulePokemonId').value);
        if (!pid) { showToast('pokemonId requerido','error'); return; }
        if (!confirm('¿Eliminar regla?')) return;
        try {
            await API.del(`/api/admin/rules/${pid}`);
            showToast('Regla eliminada','success');
            qs('#ruleOrigin').value=''; qs('#ruleAvail').value='';
            qs('#ruleBanned').value=''; qs('#ruleNotes').value='';
            status.textContent='';
        } catch {}
    });
}

/* ---------- CRUD Productos Internos ---------- */
function setupInternalProducts() {
    const form = qs('#productForm');
    const resetBtn = qs('#resetProdBtn');
    const grid = qs('#adminProductsGrid');
    if (!form || !grid) return;

    form.onsubmit = async e=>{
        e.preventDefault();
        const fd = new FormData(form);
        const id = fd.get('id');
        const body = Object.fromEntries(fd.entries());
        body.price = Number(body.price);
        try {
            if (id) {
                delete body.id;
                await API.put(`/api/products/${id}`, body);
                showToast('Producto actualizado','success');
            } else {
                await API.post('/api/products', body);
                showToast('Producto creado','success');
            }
            form.reset();
            qs('#prodId').value='';
            loadProductsGrid();
        } catch {}
    };
    resetBtn && (resetBtn.onclick = ()=>{
        form.reset();
        qs('#prodId').value='';
    });
    loadProductsGrid();

    async function loadProductsGrid() {
        grid.innerHTML='<div class="generic-card" style="padding:1rem;">Cargando...</div>';
        try {
            grid.innerHTML='';
            const cats = ['POKEMON','SPECIES','ITEMS','GENERATIONS'];
            for (const c of cats) {
                const data = await API.get(`/api/products?category=${c}&page=0&size=12`);
                (data.content||[]).forEach(p=>{
                    grid.appendChild(productAdminCard(p));
                });
            }
        } catch {
            grid.innerHTML='<div class="generic-card" style="padding:1rem;color:var(--danger)">Error cargando productos</div>';
        }
    }

    function productAdminCard(p) {
        const div = document.createElement('div');
        div.className='product-card';
        div.innerHTML = `
      <img src="${p.imageUrl||'https://placehold.co/300x160'}" alt="${escapeHTML(p.name)}">
      <div class="body">
        <h3>${escapeHTML(p.name)}</h3>
        <p>${escapeHTML((p.description||'').slice(0,60))}</p>
        <span class="price">$${Number(p.price).toFixed(2)}</span>
        <small class="badge">${p.category}</small>
        <div class="flex-row">
          <button class="btn small" data-edit="${p.id}">Editar</button>
          <button class="btn small danger outline" data-del="${p.id}">Borrar</button>
        </div>
      </div>`;
        div.querySelector('[data-edit]').onclick = ()=> fillProductForm(p);
        div.querySelector('[data-del]').onclick = ()=> deleteProduct(p.id);
        return div;
    }

    function fillProductForm(p) {
        qs('#prodId').value = p.id;
        qs('#prodName').value = p.name;
        qs('#prodPrice').value = p.price;
        qs('#prodCategory').value = p.category;
        qs('#prodImage').value = p.imageUrl||'';
        qs('#prodDesc').value = p.description||'';
        qs('#prodCountry').value = p.countryOfOrigin || '';
        showToast('Producto cargado','info');
    }

    async function deleteProduct(id) {
        if (!confirm('¿Eliminar producto?')) return;
        try {
            await API.del(`/api/products/${id}`);
            showToast('Eliminado','success');
            loadProductsGrid();
            form.reset();
            qs('#prodId').value='';
        } catch {}
    }
}

export {
    initAdminExtended
};