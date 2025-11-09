package cr.ac.una.portalwebpokeapi.config;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    public static class SessionData {
        public final String userId;
        public final String username;
        public final String role;
        private Instant expiresAt;
        public SessionData(String userId, String username, String role, Instant expiresAt) {
            this.userId = userId; this.username = username; this.role = role; this.expiresAt = expiresAt;
        }
        public Instant getExpiresAt() { return expiresAt; }
        public void renew(long seconds) { this.expiresAt = Instant.now().plusSeconds(seconds); }
    }

    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    private long defaultTimeout;

    public SessionManager(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public String create(String userId, String username, String role) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new SessionData(userId, username, role, Instant.now().plusSeconds(defaultTimeout)));
        return token;
    }

    public SessionData get(String token) {
        if (token == null) return null;
        SessionData data = sessions.get(token);
        if (data == null) return null;
        if (Instant.now().isAfter(data.getExpiresAt())) {
            sessions.remove(token);
            return null;
        }
        return data;
    }

    public boolean renew(String token, long seconds) {
        SessionData data = get(token);
        if (data == null) return false;
        data.renew(seconds);
        return true;
    }

    public void invalidate(String token) {
        if (token != null) sessions.remove(token);
    }

    public void setDefaultTimeout(long seconds) {
        if (seconds < 10) seconds = 10;
        this.defaultTimeout = seconds;
    }

    public long getDefaultTimeout() {
        return defaultTimeout;
    }
}