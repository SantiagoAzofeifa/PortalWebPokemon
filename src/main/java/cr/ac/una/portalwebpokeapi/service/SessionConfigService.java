package cr.ac.una.portalwebpokeapi.service;

import cr.ac.una.portalwebpokeapi.model.SessionConfigEntity;
import cr.ac.una.portalwebpokeapi.repository.SessionConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de la configuración global de sesión.
 *
 * Funcionalidad:
 *  - Consultar y actualizar el tiempo de expiración (timeout) de sesiones activas.
 *  - Si no existe configuración persistida, aplica un valor por defecto (600 segundos).
 */
@Service
@RequiredArgsConstructor
public class SessionConfigService {

    /** Repositorio de configuración de sesión. */
    private final SessionConfigRepository repo;

    /**
     * Obtiene el tiempo de expiración de sesión actual.
     * Si no hay configuración almacenada, devuelve 600 segundos por defecto.
     *
     * @return segundos de expiración configurados o valor por defecto.
     */
    public int currentTimeoutSeconds() {
        return repo.findTopByOrderByIdAsc()
                .map(SessionConfigEntity::getTimeoutSeconds)
                .orElse(600);
    }

    /**
     * Actualiza (o crea si no existe) la configuración de timeout de sesión.
     * Enforce mínimo de 10 segundos.
     *
     * @param seconds nuevo tiempo de expiración solicitado.
     * @return entidad persistida con el valor actualizado.
     */
    @Transactional
    public SessionConfigEntity update(int seconds) {
        if (seconds < 10) seconds = 10;
        SessionConfigEntity cfg = repo.findTopByOrderByIdAsc()
                .orElseGet(SessionConfigEntity::new);
        cfg.setTimeoutSeconds(seconds);
        return repo.save(cfg);
    }
}
