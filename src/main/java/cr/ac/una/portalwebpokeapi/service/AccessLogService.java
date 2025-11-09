package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.AccessLog;
import cr.ac.una.portalwebpokeapi.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessLogService {
    private final AccessLogRepository repo;

    public void log(Long userId, AccessLog.Type type, String ip) {
        AccessLog entry = (type == AccessLog.Type.LOGIN)
                ? AccessLog.login(userId, ip)
                : AccessLog.logout(userId, ip);
        repo.save(entry);
    }
}