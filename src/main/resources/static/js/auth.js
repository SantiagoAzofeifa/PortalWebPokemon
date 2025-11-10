// Fragmento dentro de loginForm.onsubmit, después de recibir r:
import { restartSessionCountdown } from './session.js';

// ...
const r = await API.post('/api/auth/login', body);
localStorage.setItem('sessionToken', r.token);
restartSessionCountdown(r.expiresIn); // NUEVO
// ...