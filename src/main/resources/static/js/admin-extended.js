// admin-extended.js
import API from './api.js';
import { qs, escapeHTML, showToast } from './util.js';
import { openModal, closeModal, bindModalTriggers } from './modal.js';

bindModalTriggers();
initAdminExtended();

async function initAdminExtended() {
    if (!(await verifyAdmin())) return;
    loadDashboard();
    bindSessionTimeout();
    setupAudits();
    setupUsers();
    setupGlobalOrders();
    setupRules();
    setupPsGlobal();
    setupInternalProducts(); // opcional
}

/* ----------------- Verificación ADMIN ----------------- */
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

/* ----------------- Dashboard ----------------- */
async function loadDashboard() {
    // Timeout
    try {
        const t = await API.get('/api/admin/session-timeout');
        qs('#dashTimeout').textContent = t.timeoutSeconds;
    } catch { qs('#dashTimeout').textContent='Err'; }

    // Usuarios
    try {
        const users = await API.get('/api/admin/users');
        qs('#dashUsers').textContent = users.length;
    } catch { qs('#dashUsers').textContent='Err'; }

    // Órdenes usuario actual
    try {
        const myOrders = await API.get('/api/orders/mine');
        qs('#dashOrders').textContent = myOrders.length;
    } catch { qs('#dashOrders').textContent='Err'; }

    // P‑S pendientes global (requiere endpoint /api/admin/ps?resolved=false)
    try {
        // Placeholder porque no está implementado
        qs('#dashPsPending').textContent = '—';
    } catch { qs('#dashPsPending').textContent='Err'; }
}

/* ----------------- Timeout Sesión ----------------- */
function bindSessionTimeout() {
    const form = qs('#sessionTimeoutForm');
    if (!form) return;
    form.onsubmit = async e=>{
        e.preventDefault();
        const value = Number(qs('#newTimeout').value);
        try {
            const d = await API.put('/api/admin/session-timeout', { timeoutSeconds: value });
            showToast('Timeout actualizado','success');
            qs('#dashTimeout').textContent = d.timeoutSeconds;
        } catch {}
    };
}

/* ----------------- Auditoría ----------------- */
function setupAudits() {
    const auditBody = qs('#auditBody');
    const filterForm = qs('#auditFilterForm');
    const clearBtn = qs('#auditClear');

    filterForm.onsubmit = e=> { e.preventDefault(); loadAudits(); };
    clearBtn.onclick = ()=>{
        qs('#auditUserQ').value=''; qs('#auditAction').value='';
        qs('#auditFrom').value=''; qs('#auditTo').value='';
        loadAudits();
    };
    loadAudits();

    async function loadAudits() {
        auditBody.innerHTML='<tr><td colspan="5">Cargando...</td></tr>';
        try {
            const audits = await API.get('/api/admin/audits');
            const userQ = qs('#auditUserQ').value.trim().toLowerCase();
            const act = qs('#auditAction').value.trim();
            const from = qs('#auditFrom').value? new Date(qs('#auditFrom').value).getTime(): null;
            const to = qs('#auditTo').value? new Date(qs('#auditTo').value).getTime(): null;
            const filtered = audits.filter(a=>{
                let ok = true;
                if (userQ && !a.username.toLowerCase().includes(userQ)) ok=false;
                if (act && a.action!==act) ok=false;
                const ts = new Date(a.timestamp).getTime();
                if (from && ts<from) ok=false;
                if (to && ts>to) ok=false;
                return ok;
            });
            auditBody.innerHTML='';
            filtered.forEach(a=>{
                const tr=document.createElement('tr');
                tr.innerHTML = `<td>${a.id}</td><td>${a.userId}</td><td>${escapeHTML(a.username)}</td><td>${a.action}</td><td>${a.timestamp}</td>`;
                auditBody.appendChild(tr);
            });
        } catch {
            auditBody.innerHTML='<tr><td colspan="5" style="color:var(--danger)">Error</td></tr>';
        }
    }
}

/* ----------------- Usuarios ----------------- */
function setupUsers() {
    const body = qs('#userBody');
    const filterForm = qs('#userFilterForm');
    const clearBtn = qs('#userClear');
    filterForm.onsubmit = e => { e.preventDefault(); loadUsers(); };
    clearBtn.onclick = ()=> { qs('#userQ').value=''; qs('#userRole').value=''; loadUsers(); };
    loadUsers();

    async function loadUsers() {
        body.innerHTML='<tr><td colspan="5">Cargando...</td></tr>';
        try {
            const users = await API.get('/api/admin/users');
            const q = (qs('#userQ').value||'').trim().toLowerCase();
            const role = qs('#userRole').value.trim();
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

/* ----------------- Órdenes Globales (placeholder) ----------------- */
function setupGlobalOrders() {
    const tbody = qs('#ordersGlobalBody');
    const form = qs('#orderFilterForm');
    const clearBtn = qs('#ofClear');

    form.onsubmit = e=> { e.preventDefault(); loadGlobalOrders(); };
    clearBtn.onclick = ()=>{
        qs('#ofUserId').value=''; qs('#ofStatus').value='';
        qs('#ofFrom').value=''; qs('#ofTo').value='';
        loadGlobalOrders();
    };
    loadGlobalOrders();

    async function loadGlobalOrders() {
        tbody.innerHTML='<tr><td colspan="6">Cargando...</td></tr>';
        try {
            // Requiere GET /api/admin/orders
            // Placeholder si aún no existe
            tbody.innerHTML='<tr><td colspan="6">Endpoint /api/admin/orders no implementado</td></tr>';
        } catch {
            tbody.innerHTML='<tr><td colspan="6" style="color:var(--danger)">Error</td></tr>';
        }
    }
}

/* ----------------- Reglas Pokémon ----------------- */
function setupRules() {
    const form = qs('#rulesForm');
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

    loadBtn.onclick = async ()=>{
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
    };

    delBtn.onclick = async ()=>{
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
    };
}

/* ----------------- P‑S Global (placeholder) ----------------- */
function setupPsGlobal() {
    const body = qs('#psBody');
    const form = qs('#psFilterForm');
    const clearBtn = qs('#psClear');

    form.onsubmit = e=> { e.preventDefault(); loadPsGlobal(); };
    clearBtn.onclick = ()=>{
        qs('#psOrderId').value=''; qs('#psPokemonId').value=''; qs('#psResolved').value='';
        loadPsGlobal();
    };
    loadPsGlobal();

    async function loadPsGlobal() {
        body.innerHTML='<tr><td colspan="6">Cargando...</td></tr>';
        try {
            // Requiere GET /api/admin/ps
            body.innerHTML='<tr><td colspan="6">Endpoint /api/admin/ps no implementado</td></tr>';
        } catch {
            body.innerHTML='<tr><td colspan="6" style="color:var(--danger)">Error</td></tr>';
        }
    }
}

/* ----------------- CRUD Productos Internos (opcional) ----------------- */
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
    resetBtn.onclick = ()=>{
        form.reset();
        qs('#prodId').value='';
    };
    loadProductsGrid();

    async function loadProductsGrid() {
        grid.innerHTML='<div class="generic-card" style="padding:1rem;">Cargando...</div>';
        try {
            grid.innerHTML='';
            // Si migraste a catálogo dinámico puro de PokeAPI, esto es opcional.
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