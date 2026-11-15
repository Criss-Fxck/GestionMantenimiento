package infrastructure.api;

import domain.model.Equipo;
import domain.model.RevisionPreventiva;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.EquipoService;
import service.RevisionService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/revisiones")
public class RevisionAPI {

    private final RevisionService revisionService;
    private final EquipoService equipoService;

    public RevisionAPI(RevisionService revisionService, EquipoService equipoService) {
        this.revisionService = revisionService;
        this.equipoService = equipoService;
    }

    @PostMapping
    public ResponseEntity<String> registrarRevision(@RequestBody CrearRevisionRequest request) {
        // Busca el equipo en el repositorio en memoria actual
        Equipo equipo = equipoService.obtenerTodos().stream()
                .filter(e -> e.getIdEquipo().equals(request.equipoId()))
                .findFirst()
                .orElse(new Equipo(request.equipoId(), "Equipo Desconocido", "Ubicación Desconocida"));

        RevisionPreventiva revision = new RevisionPreventiva(
                request.idRevision(),
                equipo,
                LocalDate.parse(request.fechaProgramada()),
                request.diasAvisoPrevio(),
                request.emailSupervisor()
        );

        revisionService.registrarRevision(revision);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Revisión " + request.idRevision() + " programada con éxito.");
    }

    @PutMapping("/{id}/reprogramar")
    public ResponseEntity<String> reprogramarRevision(@PathVariable String id, @RequestBody ReprogramarRequest request) {
        LocalDate nuevaFecha = LocalDate.parse(request.nuevaFecha());
        revisionService.reprogramarRevision(id, nuevaFecha);
        return ResponseEntity.ok("Revisión " + id + " reprogramada para el " + nuevaFecha + ". Se recalculó la alerta.");
    }

    public record CrearRevisionRequest(
            String idRevision,
            String equipoId,
            String fechaProgramada, // Formato esperado: YYYY-MM-DD
            int diasAvisoPrevio,
            String emailSupervisor
    ) {}

    public record ReprogramarRequest(String nuevaFecha) {}
}