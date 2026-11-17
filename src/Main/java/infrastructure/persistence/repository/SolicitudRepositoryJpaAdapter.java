package infrastructure.persistence.repository;

import domain.model.SolicitudMantenimiento;
import domain.repository.SolicitudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SolicitudRepositoryJpaAdapter implements SolicitudRepository {
    private final SolicitudJpaRepository solicitudJpaRepository;

    public SolicitudRepositoryJpaAdapter(SolicitudJpaRepository solicitudJpaRepository) {
        this.solicitudJpaRepository = solicitudJpaRepository;
    }

    @Override
    public List<SolicitudMantenimiento> obtenerTodas() {
        return solicitudJpaRepository.findAll();
    }

    @Override
    public Optional<SolicitudMantenimiento> buscarPorId(int idSolicitud) {
        return solicitudJpaRepository.findById(idSolicitud);
    }

    @Override
    public SolicitudMantenimiento guardar(SolicitudMantenimiento solicitud) {
        return solicitudJpaRepository.saveAndFlush(solicitud);
    }
}
