// api.js
import { showToast } from './util.js';

const API = {
    token() { return localStorage.getItem('sessionToken'); },
    headers(json=true) {
        const h = {};
        if (json) h['Content-Type'] = 'application/json';
        const t = API.token();
        if (t) h['X-SESSION-TOKEN'] = t;
        return h;
    },
    async get(url) {
        return handle(await fetch(url,{headers:API.headers(false)}));
    },
    async post(url, body={}) {
        return handle(await fetch(url,{method:'POST',headers:API.headers(true),body:JSON.stringify(body)}));
    },
    async put(url, body={}) {
        return handle(await fetch(url,{method:'PUT',headers:API.headers(true),body:JSON.stringify(body)}));
    },
    async del(url) {
        return handle(await fetch(url,{method:'DELETE',headers:API.headers(false)}));
    }
};

async function handle(res){
    const ct = res.headers.get('content-type')||'';
    let data;
    try {
        if (ct.includes('application/json')) data = await res.json();
        else data = await res.text();
    } catch { data=null; }
    if (!res.ok) {
        const msg = data && data.error ? data.error : (typeof data==='string'? data : 'Error en la solicitud');
        showToast(msg,'error');
        throw new Error(msg);
    }
    return data;
}

export default API;