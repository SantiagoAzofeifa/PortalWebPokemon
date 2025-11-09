package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.repository.SessionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.SessionConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionConfigService {
    private final SessionConfigRepository repo;

    public int currentTtlSeconds() {
        return repo.findAll().stream().findFirst().map(SessionConfig::getTtlSeconds).orElse(1200);
    }

    public SessionConfig getOrCreate() {
        return repo.findAll().stream().findFirst().orElseGet(() -> repo.save(SessionConfig.builder().ttlSeconds(1200).build()));
    }

    public SessionConfig update(int ttlSeconds) {
        SessionConfig s = getOrCreate();
        s.setTtlSeconds(ttlSeconds);
        return repo.save(s);
    }
}
