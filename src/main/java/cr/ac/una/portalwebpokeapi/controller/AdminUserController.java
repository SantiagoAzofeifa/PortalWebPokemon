package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.SessionConfigEntity;
import cr.ac.una.portalwebpokeapi.model.User;
import cr.ac.una.portalwebpokeapi.model.UserRole;
import cr.ac.una.portalwebpokeapi.repository.LoginAuditRepository;
import cr.ac.una.portalwebpokeapi.repository.SessionConfigRepository;
import cr.ac.una.portalwebpokeapi.repository.UserRepository;
import cr.ac.una.portalwebpokeapi.service.SessionConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/api/admin")
public class AdminUserController {
    private final LoginAuditRepository audits;
    private final SessionManager sessions;
    private final SessionConfigService sessionCfg;
    private final SessionConfigRepository sessionRepo;
    private final UserRepository users;

    public AdminUserController(LoginAuditRepository audits, SessionManager sessions,
                               SessionConfigService sessionCfg, SessionConfigRepository sessionRepo,
                               UserRepository users) {
        this.audits = audits; this.sessions = sessions; this.sessionCfg = sessionCfg; this.sessionRepo = sessionRepo; this.users=users;
    }

    private void requireAdmin(String token){
        var me = sessions.get(token);
        if (me==null) throw new SecurityException("UNAUTHORIZED");
        if (!"ADMIN".equals(me.role)) throw new SecurityException("FORBIDDEN");
    }

    @GetMapping("/audits")
    public ResponseEntity<?> audits(@RequestHeader("X-SESSION-TOKEN") String token){
        requireAdmin(token);
        return ResponseEntity.ok(audits.findAll());
    }

    @GetMapping("/session-timeout")
    public ResponseEntity<?> getTimeout(@RequestHeader("X-SESSION-TOKEN") String token){
        requireAdmin(token);
        return ResponseEntity.ok(Map.of("timeoutSeconds", sessionCfg.currentTimeoutSeconds()));
    }

    @PutMapping("/session-timeout")
    public ResponseEntity<?> setTimeout(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @RequestBody Map<String,Integer> body){
        requireAdmin(token);
        int seconds = Math.max(10, body.getOrDefault("timeoutSeconds", sessionCfg.currentTimeoutSeconds()));
        SessionConfigEntity cfg = sessionCfg.update(seconds);
        sessions.setDefaultTimeout(seconds);
        return ResponseEntity.ok(Map.of("timeoutSeconds", cfg.getTimeoutSeconds()));
    }

    // CRUD usuarios
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader("X-SESSION-TOKEN") String token){
        requireAdmin(token);
        return ResponseEntity.ok(users.findAll());
    }

    public record UserUpdateReq(String role, Boolean active){}

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @PathVariable Long id, @RequestBody UserUpdateReq req){
        requireAdmin(token);
        User u = users.findById(id).orElseThrow(()->new IllegalArgumentException("Usuario no encontrado"));
        if (req.role()!=null) u.setRole("ADMIN".equalsIgnoreCase(req.role())? UserRole.ADMIN: UserRole.USER);
        if (req.active()!=null) u.setActive(req.active());
        users.save(u);
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@RequestHeader("X-SESSION-TOKEN") String token, @PathVariable Long id){
        requireAdmin(token);
        users.deleteById(id);
        return ResponseEntity.ok(Map.of("ok",true));
    }
}