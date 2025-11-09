package cr.ac.una.portalwebpokeapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SessionAuthenticationFilter sessionAuthenticationFilter) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/css/**",
                                "/js/**",
                                "/static/**",
                                "/favicon.ico",
                                "/api/auth/**",
                                "/api/catalog/**",
                                "/api/products/**",
                                "/api/cart/**"      // lectura del carrito también permitida (token valida si hay sesión)
                        ).permitAll()
                        .anyRequest().permitAll() // Puedes cambiar a authenticated() si luego quieres restringir
                )
                .addFilterBefore(sessionAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}