import API from './api.js';
import { qs, showToast, formatMoney } from './util.js';

function initProductsPage() {
    const form = qs('#filterForm');
    const grid = qs('#productsGrid');
    const pageInfo = qs('#pageInfo');
    const prev = qs('#prevPage');
    const next = qs('#nextPage');
    let page = 0, size = 12, category = 'COMICS';

    form.onsubmit = e => {
        e.preventDefault();
        category = qs('#categorySelect').value;
        page = parseInt(qs('#pageInput').value||'0',10);
        size  = parseInt(qs('#sizeInput').value||'12',10);
        loadProducts();
    };

    prev.onclick = () => { if (page>0){ page--; qs('#pageInput').value=page; loadProducts(); } };
    next.onclick = () => { page++; qs('#pageInput').value=page; loadProducts(); };

    async function loadProducts() {
        grid.innerHTML = loaderHTML();
        try {
            const data = await API.get(`/api/products?category=${category}&page=${page}&size=${size}`);
            grid.innerHTML = '';
            data.content.forEach(p => grid.appendChild(productCard(p)));
            pageInfo.textContent = `Página ${page+1} / Total: ${data.totalElements}`;
        } catch {
            grid.innerHTML = errorHTML('Error cargando productos');
        }
    }
    loadProducts();

    const pokeForm = qs('#pokeForm');
    const pokeList = qs('#pokeList');
    if (pokeForm && pokeList) {
        pokeForm.onsubmit = async e => {
            e.preventDefault();
            const fd = new FormData(pokeForm);
            const limit = fd.get('limit');
            const offset = fd.get('offset');
            pokeList.innerHTML = loaderHTML();
            try {
                const json = await API.get(`/api/catalog/poke/list?limit=${limit}&offset=${offset}`);
                pokeList.innerHTML = '';
                (json.results || []).forEach(r => {
                    const div = document.createElement('div');
                    div.className='poke-item';
                    div.innerHTML = `<strong>${r.name}</strong><small>${r.url}</small>`;
                    pokeList.appendChild(div);
                });
            } catch {
                pokeList.innerHTML = errorHTML('Error PokeAPI');
            }
        };
    }

    function productCard(p) {
        const div = document.createElement('div');
        div.className='product-card';
        div.innerHTML = `
      <img src="${p.imageUrl||'https://placehold.co/300x160?text=Producto'}" alt="${p.name}">
      <div class="info">
        <h3>${p.name}</h3>
        <p>${escapeHTML(p.description||'Sin descripción')}</p>
        <span class="price-tag">${formatMoney(p.price)}</span>
      </div>
      <div class="actions">
        <button class="btn small primary" data-id="${p.id}">Agregar</button>
      </div>
    `;
        div.querySelector('button').onclick = () => addToCart(p.id);
        return div;
    }

    async function addToCart(productId) {
        if (!API.token()) { showToast('Inicia sesión primero','error'); return; }
        try {
            await API.post('/api/cart/items', { productId, quantity:1 });
            showToast('Agregado','success');
        } catch {}
    }

    function loaderHTML(){ return `<div class="generic-card" style="padding:1rem;">Cargando...</div>`; }
    function errorHTML(m){ return `<div class="generic-card" style="padding:1rem;color:var(--color-danger);">${m}</div>`; }
    function escapeHTML(str){ return str.replace(/[&<>"']/g, s=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[s])); }
}

async function loadCountries() {
    const grid = qs('#countriesGrid');
    if (!grid) return;
    grid.innerHTML = `<div class="generic-card" style="padding:1rem;">Cargando países...</div>`;
    try {
        const countries = await API.get('/api/catalog/countries');
        grid.innerHTML = '';
        countries.forEach(c => {
            const name = c.name?.common || c.name?.official || 'N/A';
            const flag = c.flags?.png || c.flags?.svg || '';
            const card = document.createElement('div');
            card.className='country-card';
            card.innerHTML = `
        <strong>${name}</strong>
        ${flag?`<img src="${flag}" alt="Bandera de ${name}" style="width:100%;height:80px;object-fit:contain;">`:''}
        <small>Región: ${c.region||'—'} / Subregión: ${c.subregion||'—'}</small>
        <small>Códigos: ${(c.cca2||'')} ${(c.cca3||'')}</small>
      `;
            grid.appendChild(card);
        });
    } catch {
        grid.innerHTML = `<div class="generic-card" style="padding:1rem;color:var(--color-danger);">Error cargando países</div>`;
    }
}

export { initProductsPage, loadCountries };