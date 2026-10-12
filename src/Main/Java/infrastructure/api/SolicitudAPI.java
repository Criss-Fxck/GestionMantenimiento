package infrastructure.api;

import domain.model.*;
import service.SolicitudService;
import java.util.List;

public class SolicitudAPI {
    private final SolicitudService solicitudService;

    public SolicitudAPI(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    public List<SolicitudMantenimiento> listarSolicitudes() {
        return solicitudService.obtenerTodas();
    }

    public SolicitudMantenimiento registrarSolicitud(Equipo equipo, UsuarioSolicitante solicitante, String descripcion) {
        return solicitudService.crearSolicitud(equipo, solicitante, descripcion);
    }
}