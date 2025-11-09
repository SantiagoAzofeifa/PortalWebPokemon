package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.SessionConfigEntity;
import cr.ac.una.portalwebpokeapi.repository.SessionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class SessionConfigService {
    private final SessionConfigRepository repo;

    public int currentTimeoutSeconds() {
        return repo.findTopByOrderByIdAsc().map(SessionConfigEntity::getTimeoutSeconds).orElse(600);
    }

    @Transactional
    public SessionConfigEntity update(int seconds) {
        if (seconds < 10) seconds = 10;
        SessionConfigEntity cfg = repo.findTopByOrderByIdAsc().orElseGet(SessionConfigEntity::new);
        cfg.setTimeoutSeconds(seconds);
        return repo.save(cfg);
    }
}