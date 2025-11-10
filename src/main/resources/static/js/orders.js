// orders.js
import API from './api.js';
import { qs, showToast, formatMoney, escapeHTML } from './util.js';
import { hydrateSessionUI } from './session.js';

/* ... (código previo sin cambios) ... */

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

        // 1) Guardar warehouse SIEMPRE primero
        try {
            await API.put(`/api/orders/${orderId}/warehouse`, payload);
            showToast('Datos de almacén guardados correctamente', 'success');
        } catch (err) {
            console.error('[Warehouse error]', err);
            showToast('Error al guardar datos del almacén', 'error');
            return; // no continuar si ni siquiera se guardó
        }

        // 2) (Opcional) Crear alerta por productos no chequeados.
        //    Si NO tienes endpoint, deja esto comentado.
        if (uncheckedBoxes.length > 0) {
            const failedProducts = uncheckedBoxes.map(cb => cb.value).join(',');
            try {
                // Ajusta a tu endpoint real cuando exista, por ejemplo:
                // await API.post(`/api/admin/ps`, { orderId, failedProducts });
            } catch (err) {
                console.warn('[PS warning] No se pudo registrar alerta de productos.', err);
                // No mostramos error al usuario; el guardado ya fue exitoso.
            }
        }
    });
}

/* ... (resto del archivo sin cambios, excepto este fragmento en initDeliveryPage) ... */

export async function initDeliveryPage() {
    const params = new URLSearchParams(location.search);
    const orderId = params.get('orderId');
    if (!orderId) {
        alert('Falta orderId en la URL');
        return;
    }

    const form = document.getElementById('deliveryForm');
    if (!form) return;

    try {
        const data = await API.get(`/api/orders/${orderId}`);
        if (data.delivery) {
            const d = data.delivery;
            form.method.value = d.method || '';
            form.address.value = d.address || '';
            form.scheduledDate.value = d.scheduledDate ? localDateTimeValue(d.scheduledDate) : '';
            form.trackingCode.value = d.trackingCode || '';
            form.notes.value = d.notes || '';
        }
    } catch (err) {
        console.warn('No se pudieron cargar los datos de entrega:', err);
    }

    const orderLink = document.getElementById('orderLink');
    if (orderLink) {
        orderLink.href = `order-detail.html?orderId=${orderId}`;
    }

    await handleDeliverySubmit(orderId, form);
}