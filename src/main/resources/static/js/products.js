// products.js
import API from './api.js';
import { qs, showToast, formatMoney, escapeHTML } from './util.js';

function initCatalogPage() {
    const grid = qs('#productsGrid');
    const pageInfo = qs('#pageInfo');
    const form = qs('#filterForm');
    const prev = qs('#prevPage');
    const next = qs('#nextPage');
    let page=0, size=12, category='COMICS';

    form.onsubmit = e=>{
        e.preventDefault();
        category = qs('#categorySelect').value;
        page = Number(qs('#pageInput').value||0);
        size = Number(qs('#sizeInput').value||12);
        loadProducts();
    };
    prev.onclick = ()=> { if (page>0){ page--; qs('#pageInput').value=page; loadProducts(); } };
    next.onclick = ()=> { page++; qs('#pageInput').value=page; loadProducts(); };

    async function loadProducts() {
        grid.innerHTML = loaderHTML();
        try {
            const data = await API.get(`/api/products?category=${category}&page=${page}&size=${size}`);
            grid.innerHTML = '';
            data.content.forEach(p=> grid.appendChild(productCard(p)));
            pageInfo.textContent = `Página ${page+1} | Total elementos: ${data.totalElements}`;
        } catch {
            grid.innerHTML = errorHTML('Error cargando productos');
        }
    }
    loadProducts();

    // Semillas PokeAPI
    const pokeForm = qs('#pokeForm');
    const pokeList = qs('#pokeList');
    if (pokeForm && pokeList) {
        pokeForm.onsubmit = async e=>{
            e.preventDefault();
            const fd = new FormData(pokeForm);
            const limit = fd.get('limit');
            const offset = fd.get('offset');
            pokeList.innerHTML = loaderHTML();
            try {
                const raw = await API.get(`/api/catalog/poke/list?limit=${limit}&offset=${offset}`);
                pokeList.innerHTML = '';
                (raw.results||[]).forEach(r=>{
                    const div = document.createElement('div');
                    div.className='poke-item generic-card';
                    div.style.padding='.6rem .7rem';
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
      <img src="${p.imageUrl||'https://placehold.co/300x160?text=Producto'}" alt="${escapeHTML(p.name)}">
      <div class="body">
        <h3>${escapeHTML(p.name)}</h3>
        <p>${escapeHTML((p.description||'Sin descripción').slice(0,100))}</p>
        <span class="price">${formatMoney(p.price)}</span>
        <div class="flex-row">
          <button class="btn small primary" data-id="${p.id}">Agregar</button>
        </div>
      </div>
    `;
        div.querySelector('button').onclick = ()=> addToCart(p.id);
        return div;
    }

    async function addToCart(productId) {
        if (!API.token()) { showToast('Inicia sesión','error'); return; }
        try {
            await API.post('/api/cart/items',{ productId, quantity:1 });
            showToast('Producto agregado','success');
        } catch {}
    }

    function loaderHTML(){ return `<div class="generic-card" style="padding:1rem;text-align:center">Cargando...</div>`; }
    function errorHTML(m){ return `<div class="generic-card" style="padding:1rem;text-align:center;color:var(--danger)">${m}</div>`; }
}

async function loadCountriesPage() {
    const grid = qs('#countriesGrid');
    if (!grid) return;
    grid.innerHTML = `<div class="generic-card" style="padding:1rem;">Cargando...</div>`;
    try {
        const countries = await API.get('/api/catalog/countries');
        grid.innerHTML = '';
        countries.forEach(c=>{
            const name = c.name?.common || c.name?.official || 'N/A';
            const flag = c.flags?.png || c.flags?.svg || '';
            const card = document.createElement('div');
            card.className='generic-card';
            card.style.padding='.7rem .75rem';
            card.innerHTML = `
        <strong>${escapeHTML(name)}</strong>
        ${flag? `<img src="${flag}" alt="Bandera de ${escapeHTML(name)}" style="width:100%;height:80px;object-fit:contain;">`:''}
        <small>Región: ${escapeHTML(c.region||'—')} / Subregión: ${escapeHTML(c.subregion||'—')}</small>
        <small>Códigos: ${(c.cca2||'')} ${(c.cca3||'')}</small>
      `;
            grid.appendChild(card);
        });
    } catch {
        grid.innerHTML = `<div class="generic-card" style="padding:1rem;color:var(--danger)">Error cargando países</div>`;
    }
}

export { initCatalogPage, loadCountriesPage };