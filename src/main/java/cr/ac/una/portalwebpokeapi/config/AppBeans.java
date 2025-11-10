package cr.ac.una.portalwebpokeapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración de Spring Boot.
 * Define beans que serán gestionados por el contenedor de Spring
 * y estarán disponibles para inyección en otras partes de la aplicación.
 */
@Configuration
public class AppBeans {

    /**
     * Define un bean de tipo SessionManager que controla la gestión
     * y expiración de sesiones activas dentro del sistema.
     *
     * @param timeout valor del tiempo máximo de inactividad de sesión en segundos.
     *                Se obtiene desde la propiedad 'app.session.timeout-seconds'
     *                del archivo application.properties. Si no se especifica,
     *                el valor por defecto es 600 segundos (10 minutos).
     * @return instancia configurada de SessionManager.
     */
    @Bean
    public SessionManager sessionManager(@Value("${app.session.timeout-seconds:600}") long timeout) {
        return new SessionManager(timeout);
    }
}
