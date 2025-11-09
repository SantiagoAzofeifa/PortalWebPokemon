package cr.ac.una.portalwebpokeapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Loguea TODAS las solicitudes y sus respuestas (método, ruta y status).
 * Así confirmamos si /api/orders/checkout entra siquiera al backend.
 */
@Component
public class GlobalRequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("[REQ] " + method + " " + path);
        try {
            filterChain.doFilter(request, response);
        } finally {
            System.out.println("[RES] " + method + " " + path + " -> " + response.getStatus());
        }
    }
}