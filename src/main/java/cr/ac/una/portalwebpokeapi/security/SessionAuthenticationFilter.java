package cr.ac.una.portalwebpokeapi.security;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class SessionAuthenticationFilter implements Filter {

    // nombres de atributos en HttpSession (usar constantes)
    public static final String S_ATTR_USER = "AUTH_USERNAME";
    public static final String S_ATTR_ROLE = "AUTH_ROLE";
    public static final String S_ATTR_USER_ID = "AUTH_USER_ID";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpSession session = request.getSession(false);
            if (session != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Object username = session.getAttribute(S_ATTR_USER);
                Object role = session.getAttribute(S_ATTR_ROLE);
                Object uid = session.getAttribute(S_ATTR_USER_ID);
                if (username != null && role != null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            username.toString(),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role.toString()))
                    );
                    // opcional: añadir details con userId
                    auth.setDetails(uid);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            chain.doFilter(req, res);
        } finally {
            // no limpiar authentication — la dejará Spring al finalizar request
        }
    }
}

