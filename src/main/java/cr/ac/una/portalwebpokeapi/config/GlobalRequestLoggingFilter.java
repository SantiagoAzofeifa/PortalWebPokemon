package cr.ac.una.portalwebpokeapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro global que intercepta todas las solicitudes HTTP entrantes al backend.
 *
 * Su propósito es registrar en la consola cada petición y su respuesta asociada,
 * mostrando el método HTTP, la ruta solicitada y el código de estado devuelto.
 *
 * Este filtro ayuda a depurar y verificar si ciertas rutas (por ejemplo,
 * "/api/orders/checkout") realmente están llegando al servidor, y con qué resultado.
 */
@Component
public class GlobalRequestLoggingFilter extends OncePerRequestFilter {

    /**
     * Método principal del filtro. Se ejecuta una vez por cada solicitud.
     *
     * @param request  objeto que representa la solicitud HTTP entrante.
     * @param response objeto que representa la respuesta HTTP saliente.
     * @param filterChain cadena de filtros de Spring que continúa el procesamiento.
     * @throws ServletException si ocurre un error en la cadena de filtros.
     * @throws IOException si ocurre un error de E/S durante el proceso.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Captura el método HTTP (GET, POST, etc.) y la ruta solicitada.
        String path = request.getRequestURI();
        String method = request.getMethod();

        // Log inicial al recibir la solicitud.
        System.out.println("[REQ] " + method + " " + path);

        try {
            // Continúa la ejecución del resto de la cadena de filtros y el controlador.
            filterChain.doFilter(request, response);
        } finally {
            // Log final una vez completada la respuesta, mostrando el código HTTP resultante.
            System.out.println("[RES] " + method + " " + path + " -> " + response.getStatus());
        }
    }
}
