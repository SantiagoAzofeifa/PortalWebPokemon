(function(){
    let token = localStorage.getItem('sessionToken');
    if (!token) return;

    async function renew() {
        const res = await fetch('/api/auth/renew', { method:'POST', headers: {'X-SESSION-TOKEN': token}});
        if (!res.ok) {
            alert('Sesión expirada, por favor inicia sesión nuevamente.');
            localStorage.removeItem('sessionToken');
        }
    }

    // Temporizador simple para renovar cada N-5 segundos
    // En producción, consulta /api/auth/me para saber expiración exacta y desplegar modal
    setInterval(renew, 300000); // 5 minutos
})();