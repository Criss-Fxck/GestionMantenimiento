package infrastructure.api;

import domain.model.Equipo;
import domain.model.SolicitudMantenimiento;
import domain.model.UsuarioSolicitante;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.SolicitudService;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudAPI {
    private final SolicitudService solicitudService;

    public SolicitudAPI(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudMantenimiento>> listarSolicitudes() {
        return ResponseEntity.ok(solicitudService.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<SolicitudMantenimiento> registrarSolicitud(
            @RequestBody CrearSolicitudRequest request) {
        Equipo equipo = new Equipo(
                request.equipoId(), request.equipoNombre(), request.equipoUbicacion());
        UsuarioSolicitante solicitante = new UsuarioSolicitante(
                request.solicitanteId(), request.solicitanteNombre(),
                request.departamento(), request.email());
        SolicitudMantenimiento solicitud = solicitudService.crearSolicitud(
                equipo, solicitante, request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }

    public record CrearSolicitudRequest(
            String equipoId,
            String equipoNombre,
            String equipoUbicacion,
            String solicitanteId,
            String solicitanteNombre,
            String departamento,
            String email,
            String descripcion
    ) {}
}
