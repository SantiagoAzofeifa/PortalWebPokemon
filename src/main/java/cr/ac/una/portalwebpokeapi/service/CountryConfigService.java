package cr.ac.una.portalwebpokeapi.service.config;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Configuración de países permitidos para el proyecto.
 * Lista ISO-2 restringida para simplificar validaciones y UI.
 */
@Service
public class CountryConfigService {

    private static final Set<String> ALLOWED = Set.of(
            "CR","US","MX","ES","AR","CL","BR","FR","DE","JP","CN"
    );

    public Set<String> allowed() {
        return ALLOWED;
    }

    public List<String> allowedList() {
        // Orden estable para que el mapeo determinista sea reproducible
        return ALLOWED.stream().sorted().toList();
    }

    public boolean isAllowed(String code) {
        return code != null && ALLOWED.contains(code.trim().toUpperCase());
    }
}