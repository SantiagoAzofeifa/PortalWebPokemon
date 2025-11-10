package cr.ac.una.portalwebpokeapi.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Manejador global de excepciones para controladores REST.
 *
 * Captura y traduce las excepciones comunes del backend a respuestas HTTP legibles,
 * devolviendo mensajes JSON estandarizados. También realiza trazas en consola
 * para facilitar la depuración de errores.
 */
@ControllerAdvice
public class RestExceptionHandler {

    /**
     * Maneja argumentos inválidos pasados a métodos del backend.
     *
     * @param ex excepción de tipo IllegalArgumentException.
     * @return respuesta HTTP 400 (Bad Request) con el mensaje de error.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> bad(IllegalArgumentException ex){
        System.out.println("[ERR] IllegalArgument: " + ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /**
     * Maneja violaciones de integridad referencial o restricciones únicas
     * generadas por la base de datos (por ejemplo, llaves duplicadas).
     *
     * @param ex excepción de tipo DataIntegrityViolationException.
     * @return respuesta HTTP 400 (Bad Request) con descripción del error SQL.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> integrity(DataIntegrityViolationException ex){
        System.out.println("[ERR] DataIntegrity: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(Map.of("error",
                "Violación de integridad: " + ex.getMostSpecificCause().getMessage()));
    }

    /**
     * Maneja errores en la lectura o deserialización del cuerpo JSON de la solicitud.
     * Por ejemplo, cuando el cliente envía un formato JSON inválido.
     *
     * @param ex excepción de tipo HttpMessageNotReadableException.
     * @return respuesta HTTP 400 (Bad Request) indicando JSON inválido.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> unreadable(HttpMessageNotReadableException ex){
        System.out.println("[ERR] JSON no legible: " + ex.getMostSpecificCause().getMessage());
        return ResponseEntity.badRequest().body(Map.of("error",
                "JSON inválido: " + ex.getMostSpecificCause().getMessage()));
    }

    /**
     * Maneja solicitudes que omiten encabezados HTTP requeridos
     * (por ejemplo, tokens de autenticación).
     *
     * @param ex excepción de tipo MissingRequestHeaderException.
     * @return respuesta HTTP 401 (Unauthorized) indicando el encabezado faltante.
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<?> missingHeader(MissingRequestHeaderException ex){
        System.out.println("[ERR] Header faltante: " + ex.getHeaderName());
        return ResponseEntity.status(401).body(Map.of("error",
                "Header faltante: " + ex.getHeaderName()));
    }

    /**
     * Maneja excepciones de seguridad personalizadas que representan
     * casos de autenticación o autorización fallida.
     *
     * @param ex excepción de tipo SecurityException.
     * @return respuesta HTTP 401 o 403 según el tipo de restricción violada.
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<?> sec(SecurityException ex){
        String msg = ex.getMessage();
        System.out.println("[ERR] SecurityException: " + msg);

        if ("UNAUTHORIZED".equalsIgnoreCase(msg))
            return ResponseEntity.status(401).body(Map.of("error","No autenticado"));

        if ("FORBIDDEN".equalsIgnoreCase(msg))
            return ResponseEntity.status(403).body(Map.of("error","No autorizado"));

        return ResponseEntity.status(403).body(Map.of("error",
                msg == null ? "Acceso denegado" : msg));
    }

    /**
     * Manejador genérico para cualquier otra excepción no contemplada
     * específicamente. Se utiliza como último recurso.
     *
     * @param ex cualquier excepción no manejada previamente.
     * @return respuesta HTTP 500 (Internal Server Error).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> generic(Exception ex){
        System.out.println("[ERR] Genérico: " + ex.getClass().getName() + " -> " + ex.getMessage());
        ex.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "Error interno"));
    }
}
