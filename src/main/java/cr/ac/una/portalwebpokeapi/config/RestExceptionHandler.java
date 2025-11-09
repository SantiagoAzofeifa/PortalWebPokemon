package cr.ac.una.portalwebpokeapi.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> security(SecurityException ex) {
        String msg = ex.getMessage();
        if ("UNAUTHORIZED".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(401).body(Map.of("error","No autenticado"));
        }
        if ("FORBIDDEN".equalsIgnoreCase(msg)) {
            return ResponseEntity.status(403).body(Map.of("error","No autorizado"));
        }
        return ResponseEntity.status(403).body(Map.of("error", msg == null ? "Acceso denegado" : msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("error","Error interno"));
    }
}