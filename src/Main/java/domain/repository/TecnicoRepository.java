package domain.repository;

import domain.model.Tecnico;
import java.util.List;
import java.util.Optional;

public interface TecnicoRepository {
    List<Tecnico> obtenerTodos();
    Optional<Tecnico> buscarPorId(String idUsuario);
    void guardar(Tecnico tecnico);
}