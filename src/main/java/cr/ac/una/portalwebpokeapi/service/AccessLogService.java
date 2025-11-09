package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.AccessLog;
import cr.ac.una.portalwebpokeapi.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccessLogService {
    private final AccessLogRepository repo;

    public void log(Long userId, AccessLog.Type type, String ip) {
        repo.save(AccessLog.builder()
                .userId(userId)
                .type(type)
                .ip(ip)
                .at(Instant.now())
                .build());
    }
}
