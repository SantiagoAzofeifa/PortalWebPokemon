package cr.ac.una.portalwebpokeapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeans {

    @Bean
    public SessionManager sessionManager(
            @Value("${app.session.timeout-seconds:600}") long timeoutSeconds) {
        return new SessionManager(timeoutSeconds);
    }
}