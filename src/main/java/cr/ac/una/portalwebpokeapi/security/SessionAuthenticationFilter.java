package cr.ac.una.portalwebpokeapi.security;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro personalizado de autenticación basado en sesión.
 *
 * Se ejecuta una vez por solicitud y puede usarse para validar el token de sesión
 * enviado por el cliente (header: X-SESSION-TOKEN). Actualmente solo encadena la
 * solicitud sin validación explícita, pero deja el punto de extensión preparado
 * para futuras mejoras (como auditoría o contexto de usuario).
 */
@Component("sessionAuthenticationFilter")
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    /** Gestor de sesiones activo en el backend. */
    private final SessionManager sessions;

    /**
     * Constructor que inyecta el gestor de sesiones.
     *
     * @param sessions componente que administra los tokens de sesión.
     */
    public SessionAuthenticationFilter(SessionManager sessions) {
        this.sessions = sessions;
    }

    /**
     * Ejecuta el filtro en cada solicitud HTTP entrante.
     * Actualmente no realiza validación del token, pero permite interceptar
     * futuras peticiones para auditoría o autenticación contextual.
     *
     * @param request solicitud HTTP.
     * @param response respuesta HTTP.
     * @param filterChain cadena de filtros de Spring Security.
     * @throws ServletException si ocurre un error en el filtro.
     * @throws IOException si ocurre un error de E/S.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Punto de extensión para lógica de autenticación basada en sesión.
        filterChain.doFilter(request, response);
    }
}
