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

/**
 * Controlador REST para autenticación y gestión de sesión.
 *
 * Base: /api/auth
 * Funciones:
 *  - Registro de usuarios (hash con BCrypt).
 *  - Login y creación de token de sesión en memoria.
 *  - Logout e invalidación de token.
 *  - Renovación de sesión según timeout vigente.
 *  - Endpoint /me para obtener datos del sujeto autenticado.
 *
 * Seguridad:
 *  - El token se envía en el header "X-SESSION-TOKEN".
 *  - Las sesiones se gestionan en memoria mediante SessionManager.
 *  - El timeout vigente proviene de SessionConfigService.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;                 // Acceso a usuarios
    private final LoginAuditRepository audits;          // Auditoría de eventos de login/logout
    private final SessionManager sessions;              // Gestión de tokens y expiraciones
    private final SessionConfigService sessionCfg;      // Provee timeout actual de sesión

    public AuthController(UserRepository users,
                          LoginAuditRepository audits,
                          SessionManager sessions,
                          SessionConfigService sessionCfg) {
        this.users = users;
        this.audits = audits;
        this.sessions = sessions;
        this.sessionCfg = sessionCfg;
    }

    /** Payload de registro. */
    public record RegisterReq(String username, String password, String role){}

    /** Payload de login. */
    public record LoginReq(String username, String password){}

    /**
     * Registra un usuario nuevo.
     * Valida usuario/contraseña requeridos y unicidad del username.
     * La contraseña se almacena con hash BCrypt.
     *
     * POST /api/auth/register
     *
     * @param req body con username, password y role opcional ("ADMIN" para admin).
     * @return 200 {"ok":true} o 400 con {"error":...}
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterReq req){
        if (req.username()==null || req.username().isBlank()
                || req.password()==null || req.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error","Usuario y contraseña requeridos"));
        }
        if (users.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body(Map.of("error","Usuario ya existe"));
        }

        User u = new User();
        u.setUsername(req.username());
        // Hash de contraseña con BCrypt y salt generado
        u.setPasswordHash(BCrypt.hashpw(req.password(), BCrypt.gensalt()));
        if ("ADMIN".equalsIgnoreCase(req.role())) {
            u.setRole(UserRole.ADMIN);
        }
        users.save(u);

        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Autentica credenciales y crea una sesión con token UUID.
     * Rechaza si el usuario no existe, está inactivo o el hash no coincide.
     *
     * POST /api/auth/login
     *
     * @param req body con username y password en texto plano.
     * @return 200 {token, username, role, expiresIn} o 401 {"error":"Credenciales inválidas"}.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req) {
        User u = users.findByUsername(req.username()).orElse(null);
        if (u==null || !u.isActive() || !BCrypt.checkpw(req.password(), u.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error","Credenciales inválidas"));
        }

        int ttl = sessionCfg.currentTimeoutSeconds(); // timeout efectivo
        String token = sessions.create(String.valueOf(u.getId()), u.getUsername(), u.getRole().name());
        audits.save(audit(u.getId(), u.getUsername(), "LOGIN"));

        return ResponseEntity.ok(Map.of(
                "token", token,
                "username", u.getUsername(),
                "role", u.getRole().name(),
                "expiresIn", ttl
        ));
    }

    /**
     * Cierra sesión si el token es válido, registrando LOGOUT.
     * Idempotente a nivel de API.
     *
     * POST /api/auth/logout
     *
     * @param token header X-SESSION-TOKEN.
     * @return 200 {"ok":true}
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("X-SESSION-TOKEN") String token){
        var d = sessions.get(token);
        if (d != null){
            sessions.invalidate(token);
            audits.save(audit(Long.valueOf(d.userId), d.username, "LOGOUT"));
        }
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Renueva la sesión asociada al token, aplicando el timeout vigente.
     * Falla con 401 si el token no existe o expiró.
     *
     * POST /api/auth/renew
     *
     * @param token header X-SESSION-TOKEN.
     * @return 200 {"ok":true,"expiresIn":ttl} o 401 {"error":"Sesión expirada"}.
     */
    @PostMapping("/renew")
    public ResponseEntity<?> renew(@RequestHeader("X-SESSION-TOKEN") String token){
        int ttl = sessionCfg.currentTimeoutSeconds();
        boolean ok = sessions.renew(token, ttl);
        return ok
                ? ResponseEntity.ok(Map.of("ok", true, "expiresIn", ttl))
                : ResponseEntity.status(401).body(Map.of("error","Sesión expirada"));
    }

    /**
     * Devuelve información del sujeto autenticado.
     * Incluye userId, username, role y fecha/hora de expiración.
     *
     * GET /api/auth/me
     *
     * @param token header X-SESSION-TOKEN.
     * @return 200 con datos o 401 si el token no es válido.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("X-SESSION-TOKEN") String token){
        var data = sessions.get(token);
        if (data == null) {
            return ResponseEntity.status(401).body(Map.of("error","No autenticado"));
        }
        return ResponseEntity.ok(Map.of(
                "userId", data.userId,
                "username", data.username,
                "role", data.role,
                "expiresAt", data.getExpiresAt().toString()
        ));
    }

    /**
     * Construye una entidad de auditoría con marca temporal actual.
     * @param userId id del usuario
     * @param username nombre del usuario
     * @param action "LOGIN" o "LOGOUT"
     * @return entidad lista para persistir
     */
    private LoginAudit audit(Long userId, String username, String action){
        LoginAudit a = new LoginAudit();
        a.setUserId(userId);
        a.setUsername(username);
        a.setAction(action);
        a.setTimestamp(Instant.now());
        return a;
    }
}
