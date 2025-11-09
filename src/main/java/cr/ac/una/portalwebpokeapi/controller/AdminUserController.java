package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.repository.LoginAuditRepository;
import cr.ac.una.portalwebpokeapi.service.SessionConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final LoginAuditRepository audits;
    private final SessionManager sessions;
    private final SessionConfigService sessionCfg;

    public AdminUserController(LoginAuditRepository audits, SessionManager sessions, SessionConfigService sessionCfg) {
        this.audits = audits;
        this.sessions = sessions;
        this.sessionCfg = sessionCfg;
    }

    private void requireAdmin(String token) {
        var me = sessions.get(token);
        if (me == null) throw new SecurityException("UNAUTHORIZED");
        if (!"ADMIN".equals(me.role)) throw new SecurityException("FORBIDDEN");
    }

    @GetMapping("/audits")
    public ResponseEntity<?> listAudits(@RequestHeader("X-SESSION-TOKEN") String token) {
        requireAdmin(token);
        return ResponseEntity.ok(audits.findAll());
    }

    @GetMapping("/session-timeout")
    public ResponseEntity<?> getTimeout(@RequestHeader("X-SESSION-TOKEN") String token) {
        requireAdmin(token);
        return ResponseEntity.ok(Map.of("timeoutSeconds", sessionCfg.currentTimeoutSeconds()));
    }

    @PutMapping("/session-timeout")
    public ResponseEntity<?> setTimeout(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @RequestBody Map<String,Integer> body) {
        requireAdmin(token);
        int seconds = Math.max(10, body.getOrDefault("timeoutSeconds", sessionCfg.currentTimeoutSeconds()));
        var cfg = sessionCfg.update(seconds);
        return ResponseEntity.ok(Map.of("timeoutSeconds", cfg.getTimeoutSeconds()));
    }
}