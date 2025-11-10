// admin.js
import API from './api.js';
import {qs, showToast, formatMoney, escapeHTML} from './util.js';

function initAdminPage() {
    verifyAdmin();
    loadTimeout();
    bindTimeoutForm();
    bindUserSection();
    bindProductForm();
    loadProductsGrid();
    bindAudits();
}

async function verifyAdmin() {
    try {
        const me = await API.get('/api/auth/me');
        if (me.role !== 'ADMIN') {
            showToast('Acceso sólo ADMIN', 'error');
            setTimeout(() => location.href = 'index.html', 1500);
        }
    } catch {
        showToast('Debes iniciar sesión', 'error');
        location.href = 'login.html';
    }
}

async function loadTimeout() {
    try {
        const d = await API.get('/api/admin/session-timeout');
        qs('#timeoutSeconds').value = d.timeoutSeconds;
        qs('#timeoutInfo').textContent = `Actual: ${d.timeoutSeconds}s`;
    } catch {
    }
}

function bindTimeoutForm() {
    const f = qs('#timeoutForm');
    if (!f) return;
    f.onsubmit = async e => {
        e.preventDefault();
        const secs = Number(qs('#timeoutSeconds').value);
        try {
            const d = await API.put('/api/admin/session-timeout', {timeoutSeconds: secs});
            qs('#timeoutInfo').textContent = `Actual: ${d.timeoutSeconds}s`;
            showToast('Timeout actualizado', 'success');
        } catch {
        }
    };
}

function bindUserSection() {
    const tbody = qs('#usersBody');
    if (!tbody) return;

    async function loadUsers() {
        tbody.innerHTML = '<tr><td colspan="5">Cargando...</td></tr>';
        try {
            const users = await API.get('/api/admin/rules/users');
            tbody.innerHTML = '';
            if (!users || users.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5">No hay usuarios registrados.</td></tr>';
                return;
            }
            users.forEach(u => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <tr>
  <td>${u.id}</td>
  <td>${escapeHTML(u.username)}</td>
  <td>
    <div class="role-dropdown">
      <span class="current-role">${u.role}</span>
      <select data-role="${u.id}" class="role-selector">
        <option value="USER" ${u.role === 'USER' ? 'selected' : ''}>USER</option>
        <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>ADMIN</option>
      </select>
    </div>
  </td>
  <td>${u.active ? 'Sí' : 'No'}</td>
  <td>
    <button class="btn small danger outline" data-active="${u.id}">Toggle Activo</button>
    <button class="btn small danger" data-delete="${u.id}">Eliminar</button>
  </td>
</tr>
                `;
                tbody.appendChild(tr);
            });
        } catch (err) {
            console.error('Error al cargar usuarios:', err);
            tbody.innerHTML = '<tr><td colspan="5" style="color:var(--danger)">Error al cargar</td></tr>';
        }
    }

    loadUsers();

    const reloadBtn = qs('#reloadUsersBtn');
    if (reloadBtn) reloadBtn.onclick = loadUsers;



    tbody.onchange = async e => {
        if (e.target.matches('button[data-delete]')) {
            const id = e.target.getAttribute('data-delete');
            if (!confirm('¿Eliminar usuario?')) return;
            try {
                await API.del(`/api/admin/users/${id}`);
                showToast('Usuario eliminado','success');
                reloadBtn.click();
            } catch {}
        }

        if (e.target.matches('select[data-role]')) {
            const id = e.target.getAttribute('data-role');
            const newRole = e.target.value;
            if (!confirm(`¿Cambiar rol de usuario ${id} a ${newRole}?`)) return;

            try {
                await API.put(`/api/admin/users/${id}`, { role: newRole });
                showToast(`Rol actualizado a ${newRole}`, 'success');
                reloadBtn.click();
            } catch {
                showToast('Error al cambiar el rol', 'error');
            }
        }
    };


}



function bindAudits() {
    const btn = qs('#loadAuditsBtn');
    const body = qs('#auditsBody');
    btn.onclick = async () => {
        body.innerHTML = '<tr><td colspan="5">Cargando...</td></tr>';
        try {
            const audits = await API.get('/api/admin/audits');
            body.innerHTML = '';
            audits.forEach(a => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
          <td>${a.id}</td>
          <td>${a.userId}</td>
          <td>${escapeHTML(a.username)}</td>
          <td>${escapeHTML(a.action)}</td>
          <td>${a.timestamp}</td>
        `;
                body.appendChild(tr);
            });
        } catch {
            body.innerHTML = '<tr><td colspan="5" style="color:var(--danger)">Error</td></tr>';
        }
    };
}

function bindProductForm() {
    const form = qs('#productForm');
    const resetBtn = qs('#resetProdBtn');
    form.onsubmit = async e => {
        e.preventDefault();
        const fd = new FormData(form);
        const id = fd.get('id');
        const body = Object.fromEntries(fd.entries());
        body.price = Number(body.price);
        try {
            if (id) {
                delete body.id;
                await API.put(`/api/products/${id}`, body);
                showToast('Producto actualizado', 'success');
            } else {
                await API.post('/api/products', body);
                showToast('Producto creado', 'success');
            }
            form.reset();
            qs('#prodId').value = '';
            loadProductsGrid();
        } catch {
        }
    };
    resetBtn.onclick = () => {
        form.reset();
        qs('#prodId').value = '';
    };
}

async function loadProductsGrid() {
    const grid = qs('#adminProductsGrid');
    if (!grid) return;
    grid.innerHTML = '<div class="generic-card" style="padding:1rem;">Cargando...</div>';
    try {
        grid.innerHTML = '';
        const cats = ['COMICS', 'FIGURAS', 'EVENTOS', 'SERIES'];
        for (const c of cats) {
            const data = await API.get(`/api/products?category=${c}&page=0&size=12`);
            data.content.forEach(p => {
                grid.appendChild(productAdminCard(p));
            });
        }
    } catch {
        grid.innerHTML = '<div class="generic-card" style="padding:1rem;color:var(--danger)">Error cargando productos</div>';
    }
}

function productAdminCard(p) {
    const div = document.createElement('div');
    div.className = 'product-card';
    div.innerHTML = `
    <img src="${p.imageUrl || 'https://placehold.co/300x160'}" alt="${escapeHTML(p.name)}">
    <div class="body">
      <h3>${escapeHTML(p.name)}</h3>
      <p>${escapeHTML((p.description || '').slice(0, 60))}</p>
      <span class="price">${formatMoney(p.price)}</span>
      <small class="badge">${p.category}</small>
      <div class="flex-row">
        <button class="btn small" data-edit="${p.id}">Editar</button>
        <button class="btn small danger outline" data-del="${p.id}">Borrar</button>
      </div>
    </div>
  `;
    div.querySelector('[data-edit]').onclick = () => fillProductForm(p);
    div.querySelector('[data-del]').onclick = () => deleteProduct(p.id);
    return div;
}

function fillProductForm(p) {
    qs('#prodId').value = p.id;
    qs('#prodName').value = p.name;
    qs('#prodPrice').value = p.price;
    qs('#prodCategory').value = p.category;
    qs('#prodImage').value = p.imageUrl || '';
    qs('#prodDesc').value = p.description || '';
    showToast('Producto cargado en formulario', 'info');
}

async function deleteProduct(id) {
    if (!confirm('¿Eliminar producto?')) return;
    try {
        await API.del(`/api/products/${id}`);
        showToast('Eliminado', 'success');
        loadProductsGrid();
        qs('#productForm').reset();
        qs('#prodId').value = '';
    } catch {
    }
}

async function deleteUser(id) {

}

export {initAdminPage};