package cr.ac.una.portalwebpokeapi.config;

import lombok.Getter;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clase encargada de gestionar las sesiones activas de los usuarios.
 *
 * Implementa un almacenamiento en memoria concurrente (Thread-safe) mediante
 * ConcurrentHashMap, donde cada sesión se identifica por un token UUID.
 *
 * Las sesiones incluyen datos del usuario, rol, tiempo de expiración y métodos
 * para creación, renovación e invalidación controlada.
 */
public class SessionManager {

    /**
     * Representa los datos asociados a una sesión individual de usuario.
     */
    public static class SessionData {
        public final String userId;   // ID único del usuario
        public final String username; // Nombre del usuario
        public final String role;     // Rol asignado (ej. ADMIN, USER)

        @Getter
        private Instant expiresAt;    // Momento exacto en que expira la sesión

        /**
         * Constructor de datos de sesión.
         *
         * @param userId ID del usuario autenticado.
         * @param username nombre del usuario.
         * @param role rol asignado.
         * @param expiresAt instante en que la sesión deja de ser válida.
         */
        public SessionData(String userId, String username, String role, Instant expiresAt) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.expiresAt = expiresAt;
        }

        /**
         * Devuelve los segundos restantes antes de la expiración de la sesión.
         * @return segundos restantes (0 si ya expiró).
         */
        public long getSecondsRemaining() {
            return Math.max(0, (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000);
        }

        /**
         * Renueva la sesión agregando segundos adicionales al tiempo actual.
         * @param seconds cantidad de segundos adicionales de validez.
         */
        public void renew(long seconds) {
            this.expiresAt = Instant.now().plusSeconds(seconds);
        }
    }

    // Mapa concurrente que asocia cada token con sus datos de sesión.
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    @Getter
    private long defaultTimeout; // Tiempo de expiración por defecto (en segundos)

    /**
     * Crea una instancia del gestor de sesiones con un timeout definido.
     *
     * @param defaultTimeout duración predeterminada en segundos antes de que una sesión expire.
     */
    public SessionManager(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Crea una nueva sesión y genera un token único.
     *
     * @param userId ID del usuario.
     * @param username nombre del usuario.
     * @param role rol asignado al usuario.
     * @return token UUID generado para la sesión.
     */
    public String create(String userId, String username, String role) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(userId, username, role, Instant.now().plusSeconds(defaultTimeout)));
        System.out.println("[SESSION] Creada token=" + token.substring(0,8)
                + " user=" + username + " ttl=" + defaultTimeout + "s");
        return token;
    }

    /**
     * Obtiene los datos de sesión asociados a un token.
     * Si la sesión ha expirado, se elimina automáticamente.
     *
     * @param token identificador de sesión.
     * @return datos de sesión o null si no existe o expiró.
     */
    public SessionData get(String token) {
        if (token == null) return null;
        SessionData data = sessions.get(token);
        if (data == null) return null;

        // Si la sesión ha expirado, se elimina del mapa.
        if (Instant.now().isAfter(data.getExpiresAt())) {
            System.out.println("[SESSION] Expirada token=" + token.substring(0,8)
                    + " user=" + data.username);
            sessions.remove(token);
            return null;
        }
        return data;
    }

    /**
     * Renueva el tiempo de expiración de una sesión existente.
     *
     * @param token token de sesión a renovar.
     * @param seconds nuevos segundos de duración.
     * @return true si la sesión fue renovada correctamente, false si no existe.
     */
    public boolean renew(String token, long seconds) {
        SessionData data = get(token);
        if (data == null) return false;
        data.renew(seconds);
        System.out.println("[SESSION] Renovada token=" + token.substring(0,8)
                + " nuevoTTL=" + seconds + "s");
        return true;
    }

    /**
     * Invalida una sesión eliminándola del almacenamiento.
     *
     * @param token identificador de la sesión a invalidar.
     */
    public void invalidate(String token) {
        if (token != null && sessions.remove(token) != null) {
            System.out.println("[SESSION] Invalidada token=" + token.substring(0,8));
        }
    }

    /**
     * Establece un nuevo tiempo de expiración predeterminado para las sesiones.
     *
     * @param seconds nuevo valor del tiempo de expiración (mínimo 10s).
     */
    public void setDefaultTimeout(long seconds) {
        if (seconds < 10) seconds = 10;
        this.defaultTimeout = seconds;
        System.out.println("[SESSION] Nuevo defaultTimeout=" + this.defaultTimeout);
    }
}
