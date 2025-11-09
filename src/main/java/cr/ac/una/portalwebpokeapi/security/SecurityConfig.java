package cr.ac.una.portalwebpokeapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de seguridad para la aplicación.
 *
 * Define las reglas de acceso público y privado, así como la integración del filtro
 * personalizado de autenticación basado en sesión. Deshabilita CSRF para permitir
 * el uso de peticiones REST desde el frontend.
 */
@Configuration
public class SecurityConfig {

    /**
     * Define la cadena de filtros de seguridad que controla las rutas accesibles
     * y aplica el filtro de sesión antes del filtro de autenticación por usuario/contraseña.
     *
     * @param http configuración de seguridad HTTP.
     * @param sessionAuthenticationFilter filtro de autenticación personalizada por token de sesión.
     * @return {@link SecurityFilterChain} configurado.
     * @throws Exception si ocurre un error en la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SessionAuthenticationFilter sessionAuthenticationFilter) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Se desactiva CSRF por tratarse de una API REST
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas accesibles sin autenticación
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico",
                                "/login.html",
                                "/catalog.html",
                                "/countries.html",
                                "/Cart.html",
                                "/admin.html",
                                "/order*.html",
                                "/api/auth/**",
                                "/api/catalog/**",
                                "/api/products/**",
                                "/api/cart/**"
                        ).permitAll()
                        // El resto de las rutas también están permitidas (ajustable según políticas futuras)
                        .anyRequest().permitAll()
                )
                // Inserta el filtro de sesión antes del filtro estándar de autenticación
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
