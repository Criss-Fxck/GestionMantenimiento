package infrastructure.api;

import domain.model.Equipo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.EquipoService;

import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class EquipoAPI {
    private final EquipoService equipoService;

    public EquipoAPI(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping
    public ResponseEntity<List<Equipo>> listarEquipos() {
        return ResponseEntity.ok(equipoService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<Equipo> crearEquipo(@RequestBody CrearEquipoRequest request) {
        Equipo equipo = equipoService.registrarEquipo(
                request.idEquipo(), request.nombre(), request.ubicacion());
        return ResponseEntity.status(HttpStatus.CREATED).body(equipo);
    }

    public record CrearEquipoRequest(String idEquipo, String nombre, String ubicacion) {}
}
