package infrastructure.api;

import domain.model.Equipo;
import service.EquipoService;
import java.util.List;

public class EquipoAPI {
    private final EquipoService equipoService;

    public EquipoAPI(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    public List<Equipo> listarEquipos() {
        return equipoService.obtenerTodos();
    }

    public Equipo crearEquipo(String id, String nombre, String ubicacion) {
        return equipoService.registrarEquipo(id, nombre, ubicacion);
    }
}