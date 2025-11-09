package cr.ac.una.portalwebpokeapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación Portal Web PokeAPI.
 *
 * Carga automática de todos los componentes Spring Boot:
 *  - Controladores REST
 *  - Servicios
 *  - Repositorios JPA
 *  - Configuración de seguridad
 *
 * Ejecutar para iniciar el backend en el puerto configurado (por defecto 8080).
 */
@SpringBootApplication
public class PortalWebPokeApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalWebPokeApiApplication.class, args);
    }
}
