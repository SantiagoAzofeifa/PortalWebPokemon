package cr.ac.una.portalwebpokeapi.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> bad(IllegalArgumentException ex){
        System.out.println("[ERR] IllegalArgument: " + ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integrity(DataIntegrityViolationException ex){
        System.out.println("[ERR] DataIntegrity: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(Map.of("error","Violación de integridad: " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> unreadable(HttpMessageNotReadableException ex){
        System.out.println("[ERR] JSON no legible: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(Map.of("error","JSON inválido: " + ex.getMostSpecificCause().getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<?> missingHeader(MissingRequestHeaderException ex){
        System.out.println("[ERR] Header faltante: " + ex.getHeaderName());
        return ResponseEntity.status(401).body(Map.of("error","Header faltante: " + ex.getHeaderName()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> sec(SecurityException ex){
        String msg = ex.getMessage();
        System.out.println("[ERR] SecurityException: " + msg);
        if ("UNAUTHORIZED".equalsIgnoreCase(msg))
            return ResponseEntity.status(401).body(Map.of("error","No autenticado"));
        if ("FORBIDDEN".equalsIgnoreCase(msg))
            return ResponseEntity.status(403).body(Map.of("error","No autorizado"));
        return ResponseEntity.status(403).body(Map.of("error", msg==null?"Acceso denegado":msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception ex){
        System.out.println("[ERR] Genérico: " + ex.getClass().getName() + " -> " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error","Error interno"));
    }
}