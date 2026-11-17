package domain.repository;

import domain.model.Equipo;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository {
    List<Equipo> obtenerTodos();

    Optional<Equipo> buscarPorId(String idEquipo);

    Equipo guardar(Equipo equipo);
}
