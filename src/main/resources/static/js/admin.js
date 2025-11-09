import API from './api.js';
import { qs, showToast, formatMoney } from './util.js';

function initAdminPage() {
    verifyAdminAccess();
    loadTimeout();
    loadProductsAdmin();
    qs('#timeoutForm').onsubmit = updateTimeout;
    qs('#loadAuditsBtn').onclick = loadAudits;
    qs('#productForm').onsubmit = onProductSubmit;
    qs('#resetProdBtn').onclick = resetProductForm;
}

function verifyAdminAccess() {
    API.get('/api/auth/me')
        .then(d=>{
            if (d.role !== 'ADMIN') {
                showToast('Acceso solo para ADMIN','error');
                setTimeout(()=> location.href='index.html', 1200);
            }
        })
        .catch(()=>{
            showToast('Debes iniciar sesión','error');
            location.href='login.html';
        });
}

async function loadTimeout() {
    try {
        const data = await API.get('/api/admin/session-timeout');
        qs('#timeoutInfo').textContent = `Actual: ${data.timeoutSeconds}s`;
        qs('#timeoutSeconds').value = data.timeoutSeconds;
    } catch {}
}

async function updateTimeout(e) {
    e.preventDefault();
    const val = parseInt(qs('#timeoutSeconds').value);
    try {
        const d = await API.put('/api/admin/session-timeout', { timeoutSeconds: val });
        showToast('Timeout actualizado','success');
        qs('#timeoutInfo').textContent = `Actual: ${d.timeoutSeconds}s`;
    } catch {}
}

async function loadAudits() {
    const body = qs('#auditsBody');
    body.innerHTML = '<tr><td colspan="5">Cargando...</td></tr>';
    try {
        const audits = await API.get('/api/admin/audits');
        body.innerHTML = '';
        audits.forEach(a=>{
            const tr = document.createElement('tr');
            tr.innerHTML = `
        <td>${a.id}</td>
        <td>${a.userId}</td>
        <td>${a.username}</td>
        <td>${a.action}</td>
        <td>${a.timestamp}</td>
      `;
            body.appendChild(tr);
        });
    } catch {
        body.innerHTML='<tr><td colspan="5">Error</td></tr>';
    }
}

async function loadProductsAdmin() {
    const grid = qs('#adminProductsGrid');
    grid.innerHTML = '<div class="generic-card" style="padding:1rem;">Cargando...</div>';
    try {
        // Carga primera página cada categoría
        const cats = ['COMICS','FIGURAS','EVENTOS','SERIES'];
        grid.innerHTML = '';
        for (const c of cats) {
            const data = await API.get(`/api/products?category=${c}&page=0&size=8`);
            data.content.forEach(p => grid.appendChild(productAdminCard(p)));
        }
    } catch {
        grid.innerHTML = '<div class="generic-card" style="padding:1rem;color:var(--color-danger);">Error cargando productos</div>';
    }
}

function productAdminCard(p) {
    const div = document.createElement('div');
    div.className='product-card';
    div.innerHTML = `
    <img src="${p.imageUrl||'https://placehold.co/300x160'}" alt="${p.name}">
    <div class="info">
      <h3>${p.name}</h3>
      <p>${(p.description||'').substring(0,60)}</p>
      <span class="price-tag">${formatMoney(p.price)}</span>
      <small>${p.category}</small>
    </div>
    <div class="actions">
      <button class="btn small" data-edit="${p.id}">Editar</button>
      <button class="btn small danger outline" data-del="${p.id}">Borrar</button>
    </div>
  `;
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
    showToast('Producto cargado en el formulario','info');
}

async function deleteProduct(id) {
    if (!confirm('¿Borrar este producto?')) return;
    try {
        await API.del(`/api/products/${id}`);
        showToast('Producto eliminado','success');
        loadProductsAdmin();
        resetProductForm();
    } catch {}
}

function resetProductForm() {
    qs('#productForm').reset();
    qs('#prodId').value='';
}

async function onProductSubmit(e) {
    e.preventDefault();
    const fd = new FormData(e.target);
    const body = Object.fromEntries(fd.entries());
    body.price = parseFloat(body.price);
    body.id = body.id || null;
    try {
        if (body.id) {
            const id = body.id;
            delete body.id;
            await API.put(`/api/products/${id}`, body);
            showToast('Producto actualizado','success');
        } else {
            await API.post('/api/products', body);
            showToast('Producto creado','success');
        }
        loadProductsAdmin();
        resetProductForm();
    } catch {}
}

export { initAdminPage };