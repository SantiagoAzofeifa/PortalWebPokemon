package cr.ac.una.portalwebpokeapi.security;

import cr.ac.una.portalwebpokeapi.config.SessionManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component("sessionAuthenticationFilter")
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private final SessionManager sessions;

    public SessionAuthenticationFilter(SessionManager sessions) {
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // (Opcional) lógica para marcar contextos
        filterChain.doFilter(request, response);
    }
}