import API from './api.js';
import { qs, showToast, formatMoney, escapeHTML } from './util.js';

const TYPES = ['normal','fire','water','grass','electric','ice','fighting','poison','ground','flying','psychic','bug','rock','ghost','dragon','dark','steel','fairy'];

function initCatalogPage() {
    const grid = qs('#productsGrid');
    const pageInfo = qs('#pageInfo');
    const form = qs('#filterForm');
    const prev = qs('#prevPage');
    const next = qs('#nextPage');
    const catSel = qs('#categorySelect');
    const typeSel = qs('#typeSelect');
    const typeField = qs('#typeField');

    if (typeSel && !typeSel.children.length) {
        const opt0 = document.createElement('option'); opt0.value=''; opt0.textContent='(Todos)';
        typeSel.appendChild(opt0);
        TYPES.forEach(t=> {
            const o = document.createElement('option'); o.value=t; o.textContent=t;
            typeSel.appendChild(o);
        });
    }

    let page=0, size=12, query='', type='', category='ALL';

    catSel.onchange = ()=>{
        category = catSel.value;
        typeField.style.display = category==='POKEMON' ? '' : 'none';
    };

    form.onsubmit = e=>{
        e.preventDefault();
        query = (qs('#queryInput')?.value||'').trim();
        type = (typeSel?.value||'').trim();
        page = Number(qs('#pageInput').value||0);
        size = Number(qs('#sizeInput').value||12);
        category = (catSel?.value||'ALL').trim();
        load();
    };

    prev.onclick = ()=> { if (page>0){ page--; qs('#pageInput').value=page; load(); } };
    next.onclick = ()=> { page++; qs('#pageInput').value=page; load(); };

    async function load() {
        grid.innerHTML = loaderHTML();
        try {
            const offset = page*size;
            const url = `/api/catalog/cards?limit=${size}&offset=${offset}` +
                (query? `&query=${encodeURIComponent(query)}`:'') +
                (category? `&category=${encodeURIComponent(category)}`:'') +
                (category==='POKEMON' && type? `&type=${encodeURIComponent(type)}`:'');
            const rows = await API.get(url);
            grid.innerHTML = '';
            (rows||[]).forEach(card=> grid.appendChild(cardView(card)));
            pageInfo.textContent = `Página ${page+1} | Mostrando ${rows.length}`;
        } catch {
            grid.innerHTML = errorHTML('Error cargando catálogo');
        }
    }
    load();

    function cardView(c) {
        const div = document.createElement('div');
        div.className='product-card';
        const img = c.image || 'https://placehold.co/300x160?text=' + encodeURIComponent(c.kind);
        const badges = (c.kind==='POKEMON' ? (c.types||[]) : [c.kind]).map(t=>`<span class="badge small">${escapeHTML(t)}</span>`).join(' ');
        const origin = c.country ? `<span class="badge small accent">Origen: ${escapeHTML(c.country)}</span>` : '';
        div.innerHTML = `
      <img src="${img}" alt="${escapeHTML(c.name)}">
      <div class="body">
        <h3>${escapeHTML(cap(c.name))}</h3>
        <div class="flex-row" style="gap:.35rem; flex-wrap:wrap;">
          ${badges} ${origin}
        </div>
        <span class="price">${formatMoney(c.price)}</span>
        <div class="flex-row">
          <button class="btn small primary" data-name="${c.name}" data-kind="${c.kind}">Agregar</button>
        </div>
      </div>
    `;
        div.querySelector('button').onclick = (e)=> addToCart(e.target.getAttribute('data-kind'), c.name);
        return div;
    }

    async function addToCart(kind, name) {
        if (!API.token()) { showToast('Inicia sesión','error'); return; }
        try {
            await API.post('/api/cart/catalog', { category: kind, nameOrId: name, quantity: 1 });
            showToast(`${cap(name)} (${kind}) agregado al carrito`,'success');
        } catch (err) {
            // Muestra el mensaje de backend (por ejemplo, país no coincide)
            const msg = err?.message || 'No se pudo agregar';
            showToast(msg, 'error');
        }
    }

    function loaderHTML(){ return `<div class="generic-card" style="padding:1rem;text-align:center">Cargando...</div>`; }
    function errorHTML(m){ return `<div class="generic-card" style="padding:1rem;text-align:center;color:var(--danger)">${m}</div>`; }
    function cap(s=''){ s=s.replace(/-/g,' '); return s.charAt(0).toUpperCase()+s.slice(1); }
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