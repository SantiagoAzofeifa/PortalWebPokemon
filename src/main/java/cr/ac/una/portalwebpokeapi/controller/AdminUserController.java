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

/**
 * Endpoints administrativos protegidos por rol ADMIN.
 * Base: /api/admin
 * - Auditorías de login
 * - Lectura/actualización del timeout de sesión
 * - CRUD mínimo de usuarios (listar, actualizar rol/activo, eliminar)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {

    // Repositorio de auditorías de login
    private final LoginAuditRepository audits;
    // Gestor de sesiones en memoria (valida token/rol)
    private final SessionManager sessions;
    // Servicio que resuelve el timeout efectivo y actualiza configuración
    private final SessionConfigService sessionCfg;
    // Repositorio para persistir configuración de sesión
    private final SessionConfigRepository sessionRepo;
    // Repositorio de usuarios
    private final UserRepository users;

    public AdminUserController(LoginAuditRepository audits,
                               SessionManager sessions,
                               SessionConfigService sessionCfg,
                               SessionConfigRepository sessionRepo,
                               UserRepository users) {
        this.audits = audits;
        this.sessions = sessions;
        this.sessionCfg = sessionCfg;
        this.sessionRepo = sessionRepo;
        this.users = users;
    }

    /**
     * Verifica token válido y rol ADMIN.
     * Lanza SecurityException con mensajes esperados por RestExceptionHandler.
     * @param token header X-SESSION-TOKEN
     */
    private void requireAdmin(String token){
        var me = sessions.get(token);
        if (me == null) throw new SecurityException("UNAUTHORIZED");
        if (!"ADMIN".equals(me.role)) throw new SecurityException("FORBIDDEN");
    }

    /**
     * Lista todas las auditorías de login.
     * GET /api/admin/audits
     */
    @GetMapping("/audits")
    public ResponseEntity<?> audits(@RequestHeader("X-SESSION-TOKEN") String token){
        requireAdmin(token);
        return ResponseEntity.ok(audits.findAll());
    }

    /**
     * Devuelve el timeout de sesión vigente en segundos.
     * GET /api/admin/session-timeout
     */
    @GetMapping("/session-timeout")
    public ResponseEntity<?> getTimeout(@RequestHeader("X-SESSION-TOKEN") String token){
        requireAdmin(token);
        return ResponseEntity.ok(Map.of("timeoutSeconds", sessionCfg.currentTimeoutSeconds()));
    }

    /**
     * Actualiza el timeout de sesión. Mínimo 10s.
     * Persiste en DB y ajusta el SessionManager en memoria.
     * PUT /api/admin/session-timeout
     * Body: {"timeoutSeconds": <int>}
     */
    @PutMapping("/session-timeout")
    public ResponseEntity<?> setTimeout(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @RequestBody Map<String,Integer> body){
        requireAdmin(token);
        int seconds = Math.max(10, body.getOrDefault("timeoutSeconds", sessionCfg.currentTimeoutSeconds()));
        SessionConfigEntity cfg = sessionCfg.update(seconds);   // persiste
        sessions.setDefaultTimeout(seconds);                    // aplica en runtime
        return ResponseEntity.ok(Map.of("timeoutSeconds", cfg.getTimeoutSeconds()));
    }

    // -------- Usuarios --------

    /**
     * Lista todos los usuarios.
     * GET /api/admin/users
     */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader("X-SESSION-TOKEN") String token){
        requireAdmin(token);
        return ResponseEntity.ok(users.findAll());
    }

    /**
     * DTO para actualización parcial de usuario.
     * role: "ADMIN" o cualquier otro -> USER
     * active: habilitado/deshabilitado
     */
    public record UserUpdateReq(String role, Boolean active){}

    /**
     * Actualiza rol y/o flag activo del usuario indicado.
     * PUT /api/admin/users/{id}
     * Body: {"role":"ADMIN|USER","active":true|false}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @PathVariable Long id,
                                        @RequestBody UserUpdateReq req){
        requireAdmin(token);
        User u = users.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (req.role() != null) {
            u.setRole("ADMIN".equalsIgnoreCase(req.role()) ? UserRole.ADMIN : UserRole.USER);
        }
        if (req.active() != null) {
            u.setActive(req.active());
        }

        users.save(u);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    /**
     * Elimina un usuario por id. Idempotente a nivel de API.
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@RequestHeader("X-SESSION-TOKEN") String token,
                                        @PathVariable Long id){
        requireAdmin(token);
        users.deleteById(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
