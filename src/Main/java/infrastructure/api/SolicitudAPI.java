package infrastructure.api;

import domain.model.Equipo;
import domain.model.SolicitudMantenimiento;
import domain.model.UsuarioSolicitante;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.EquipoService;
import service.SolicitudService;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudAPI {
    private final SolicitudService solicitudService;
    private final EquipoService equipoService;

    public SolicitudAPI(SolicitudService solicitudService) {
        this(solicitudService, null);
    }

    @Autowired
    public SolicitudAPI(SolicitudService solicitudService, EquipoService equipoService) {
        this.solicitudService = solicitudService;
        this.equipoService = equipoService;
    }

    @GetMapping
    public ResponseEntity<List<SolicitudMantenimiento>> listarSolicitudes() {
        return ResponseEntity.ok(solicitudService.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<SolicitudMantenimiento> registrarSolicitud(
            @RequestBody CrearSolicitudRequest request) {
        Equipo equipo = (equipoService != null && request.equipoId() != null && !request.equipoId().isBlank())
                ? equipoService.obtenerORegistrarEquipo(request.equipoId(), request.equipoNombre(), request.equipoUbicacion())
                : new Equipo(request.equipoId(), request.equipoNombre(), request.equipoUbicacion());

        UsuarioSolicitante solicitante = new UsuarioSolicitante(
                request.solicitanteId(), request.solicitanteNombre(),
                request.departamento(), request.email());

        SolicitudMantenimiento solicitud = solicitudService.crearSolicitud(
                equipo, solicitante, request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitud);
    }

    @PostMapping("/{id}/cerrar")
    public ResponseEntity<SolicitudMantenimiento> cerrarSolicitud(
            @PathVariable int id, @RequestBody CerrarSolicitudRequest request) {
        return ResponseEntity.ok(solicitudService.cerrarSolicitud(
                id, request.diagnostico(), request.solucion()));
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

    public record CerrarSolicitudRequest(String diagnostico, String solucion) {}
}