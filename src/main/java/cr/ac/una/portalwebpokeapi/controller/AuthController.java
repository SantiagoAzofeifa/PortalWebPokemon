package cr.ac.una.portalwebpokeapi.controller;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import cr.ac.una.portalwebpokeapi.model.LoginAudit;
import cr.ac.una.portalwebpokeapi.model.User;
import cr.ac.una.portalwebpokeapi.model.UserRole;
import cr.ac.una.portalwebpokeapi.repository.LoginAuditRepository;
import cr.ac.una.portalwebpokeapi.repository.UserRepository;
import cr.ac.una.portalwebpokeapi.service.SessionConfigService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final LoginAuditRepository audits;
    private final SessionManager sessions;
    private final SessionConfigService sessionCfg;

    public AuthController(UserRepository users, LoginAuditRepository audits, SessionManager sessions, SessionConfigService sessionCfg) {
        this.users = users; this.audits = audits; this.sessions = sessions; this.sessionCfg = sessionCfg;
    }

    public record RegisterReq(String username,String password,String role){}
    public record LoginReq(String username,String password){}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq req){
        if (req.username()==null || req.username().isBlank() || req.password()==null || req.password().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error","Usuario y contraseña requeridos"));
        if (users.existsByUsername(req.username()))
            return ResponseEntity.badRequest().body(Map.of("error","Usuario ya existe"));
        User u = new User();
        u.setUsername(req.username());
        u.setPasswordHash(BCrypt.hashpw(req.password(),BCrypt.gensalt()));
        if ("ADMIN".equalsIgnoreCase(req.role())) u.setRole(UserRole.ADMIN);
        users.save(u);
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        User u = users.findByUsername(req.username()).orElse(null);
        if (u==null || !u.isActive() || !BCrypt.checkpw(req.password(), u.getPasswordHash()))
            return ResponseEntity.status(401).body(Map.of("error","Credenciales inválidas"));
        int ttl = sessionCfg.currentTimeoutSeconds();
        String token = sessions.create(String.valueOf(u.getId()), u.getUsername(), u.getRole().name());
        audits.save(audit(u.getId(), u.getUsername(), "LOGIN"));
        return ResponseEntity.ok(Map.of("token",token,"username",u.getUsername(),"role",u.getRole().name(),"expiresIn",ttl));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("X-SESSION-TOKEN") String token){
        var d = sessions.get(token);
        if (d!=null){
            sessions.invalidate(token);
            audits.save(audit(Long.valueOf(d.userId), d.username, "LOGOUT"));
        }
        return ResponseEntity.ok(Map.of("ok",true));
    }

    @PostMapping("/renew")
    public ResponseEntity<?> renew(@RequestHeader("X-SESSION-TOKEN") String token){
        int ttl = sessionCfg.currentTimeoutSeconds();
        boolean ok = sessions.renew(token, ttl);
        return ok? ResponseEntity.ok(Map.of("ok",true,"expiresIn",ttl))
                : ResponseEntity.status(401).body(Map.of("error","Sesión expirada"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("X-SESSION-TOKEN") String token){
        var data = sessions.get(token);
        if (data==null) return ResponseEntity.status(401).body(Map.of("error","No autenticado"));
        return ResponseEntity.ok(Map.of("userId",data.userId,"username",data.username,"role",data.role,"expiresAt",data.getExpiresAt().toString()));
    }

    private LoginAudit audit(Long userId, String username, String action){
        LoginAudit a = new LoginAudit();
        a.setUserId(userId); a.setUsername(username); a.setAction(action); a.setTimestamp(Instant.now());
        return a;
    }
}