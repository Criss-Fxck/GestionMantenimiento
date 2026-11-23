package infrastructure.api;

import domain.model.Equipo;
import domain.model.RevisionPreventiva;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.EquipoService;
import service.RevisionService;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/revisiones")
public class RevisionAPI {

    private final RevisionService revisionService;
    private final EquipoService equipoService;

    public RevisionAPI(RevisionService revisionService, EquipoService equipoService) {
        this.revisionService = revisionService;
        this.equipoService = equipoService;
    }

    @GetMapping
    public ResponseEntity<List<RevisionPreventiva>> listarRevisiones() {
        return ResponseEntity.ok(revisionService.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<String> registrarRevision(@RequestBody CrearRevisionRequest request) {
        Equipo equipo = equipoService.buscarPorId(request.equipoId())
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe el equipo " + request.equipoId() + "."));

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
