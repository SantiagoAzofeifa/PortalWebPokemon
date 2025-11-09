/*
package cr.ac.una.portalwebpokeapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final SessionConfigRepository sessionRepo;

    @Bean
    CommandLineRunner seed() {
        return args -> {
            if (userRepo.findByUsername("admin").isEmpty()) {
                userRepo.save(User.builder()
                        .username("admin")
                        .email("admin@local")
                        .passwordHash(encoder.encode("admin123"))
                        .role(User.Role.ADMIN)
                        .active(true)
                        .build());
                System.out.println("Admin creado: admin/admin123");
            }
            if (sessionRepo.findAll().isEmpty()) {
                sessionRepo.save(SessionConfig.builder().ttlSeconds(1200).build());
            }
        };
    }
}
*/
