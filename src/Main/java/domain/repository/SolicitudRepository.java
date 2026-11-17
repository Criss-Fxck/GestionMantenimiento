package domain.repository;

import domain.model.SolicitudMantenimiento;

import java.util.List;
import java.util.Optional;

public interface SolicitudRepository {
    List<SolicitudMantenimiento> obtenerTodas();

    Optional<SolicitudMantenimiento> buscarPorId(int idSolicitud);

    SolicitudMantenimiento guardar(SolicitudMantenimiento solicitud);
}
