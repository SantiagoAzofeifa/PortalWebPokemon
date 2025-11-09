package cr.ac.una.portalwebpokeapi.config;

import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    public static class SessionData {
        public final String userId;
        public final String username;
        public final String role;
        @Getter
        private Instant expiresAt;
        public SessionData(String userId, String username, String role, Instant expiresAt) {
            this.userId = userId; this.username = username; this.role = role; this.expiresAt = expiresAt;
        }

        public long getSecondsRemaining() {
            return Math.max(0, (expiresAt.toEpochMilli() - System.currentTimeMillis()) / 1000);
        }
        public void renew(long seconds) {
            this.expiresAt = Instant.now().plusSeconds(seconds);
        }
    }

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    @Getter
    private long defaultTimeout;

    public SessionManager(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public String create(String userId, String username, String role) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(userId, username, role, Instant.now().plusSeconds(defaultTimeout)));
        System.out.println("[SESSION] Creada token=" + token.substring(0,8) + " user=" + username + " ttl=" + defaultTimeout + "s");
        return token;
    }

    public SessionData get(String token) {
        if (token == null) return null;
        SessionData data = sessions.get(token);
        if (data == null) return null;
        if (Instant.now().isAfter(data.getExpiresAt())) {
            System.out.println("[SESSION] Expirada token=" + token.substring(0,8) + " user=" + data.username);
            sessions.remove(token);
            return null;
        }
        return data;
    }

    public boolean renew(String token, long seconds) {
        SessionData data = get(token);
        if (data == null) return false;
        data.renew(seconds);
        System.out.println("[SESSION] Renovada token=" + token.substring(0,8) + " nuevoTTL=" + seconds + "s");
        return true;
    }

    public void invalidate(String token) {
        if (token != null && sessions.remove(token) != null) {
            System.out.println("[SESSION] Invalidada token=" + token.substring(0,8));
        }
    }

    public void setDefaultTimeout(long seconds) {
        if (seconds < 10) seconds = 10;
        this.defaultTimeout = seconds;
        System.out.println("[SESSION] Nuevo defaultTimeout=" + this.defaultTimeout);
    }

}