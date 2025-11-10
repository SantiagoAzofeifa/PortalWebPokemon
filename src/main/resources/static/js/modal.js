// modal.js
function openModal(id) {
    const tmpl = document.querySelector(`#${id}`);
    if (!tmpl) return;
    const wrap = document.createElement('div');
    wrap.className='modal-backdrop';
    wrap.dataset.modalInstance=id;
    wrap.innerHTML = `
    <div class="modal" role="dialog" aria-modal="true">
      <header>
        <h3>${tmpl.dataset.title||'Modal'}</h3>
        <button class="btn small danger outline" data-close type="button">✕</button>
      </header>
      <div class="modal-body">${tmpl.innerHTML}</div>
    </div>`;
    document.body.appendChild(wrap);
    wrap.addEventListener('click', e=>{
        if (e.target.matches('[data-close]') || e.target===wrap) closeModal(id);
    });
}
function closeModal(id) {
    const inst = document.querySelector(`.modal-backdrop[data-modal-instance="${id}"]`);
    if (inst) inst.remove();
}
function bindModalTriggers() {
    document.addEventListener('click', e=>{
        if (e.target.matches('[data-modal-open]')) {
            const target = e.target.getAttribute('data-modal-open');
            openModal(target);
        }
    });
}
export { openModal, closeModal, bindModalTriggers };