package cr.ac.una.portalwebpokeapi.controller;


import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.SessionConfigEntity;
import cr.ac.una.portalwebpokeapi.repository.LoginAuditRepository;
import cr.ac.una.portalwebpokeapi.repository.SessionConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    private final SessionConfigRepository sessionConfigs;
    private final LoginAuditRepository audits;
    private final SessionManager sessions;

    @Value("${app.session.timeout-seconds}")
    private long defaultTimeout;

    public AdminUserController(SessionConfigRepository sessionConfigs, LoginAuditRepository audits, SessionManager sessions) {
        this.sessionConfigs = sessionConfigs;
        this.audits = audits;
        this.sessions = sessions;
    }

    @GetMapping("/audits")
    public ResponseEntity<?> listAudits(@RequestHeader("X-SESSION-TOKEN") String token) {
        var me = sessions.get(token);
        if (me == null || !"ADMIN".equals(me.role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(audits.findAll());
    }

    @GetMapping("/session-timeout")
    public ResponseEntity<?> getTimeout(@RequestHeader("X-SESSION-TOKEN") String token) {
        var me = sessions.get(token);
        if (me == null || !"ADMIN".equals(me.role)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(
                sessionConfigs.findAll().stream().findFirst()
                        .map(cfg -> Map.of("timeoutSeconds", cfg.getTimeoutSeconds()))
                        .orElse(Map.of("timeoutSeconds", (int) defaultTimeout))
        );
    }

    @PutMapping("/session-timeout")
    public ResponseEntity<?> setTimeout(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @RequestBody Map<String,Integer> body) {
        var me = sessions.get(token);
        if (me == null || !"ADMIN".equals(me.role)) return ResponseEntity.status(403).build();
        int seconds = Math.max(10, body.getOrDefault("timeoutSeconds", (int) defaultTimeout));
        var cfg = sessionConfigs.findAll().stream().findFirst().orElseGet(SessionConfigEntity::new);
        cfg.setTimeoutSeconds(seconds);
        sessionConfigs.save(cfg);
        return ResponseEntity.ok(Map.of("timeoutSeconds", seconds));
    }
}