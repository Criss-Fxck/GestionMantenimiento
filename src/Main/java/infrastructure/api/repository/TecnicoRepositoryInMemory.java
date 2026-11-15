package infrastructure.repository;

import domain.model.Tecnico;
import domain.repository.TecnicoRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TecnicoRepositoryInMemory implements TecnicoRepository {

    private final Map<String, Tecnico> baseDeDatos = new ConcurrentHashMap<>();

    public TecnicoRepositoryInMemory() {
        // Datos de prueba
        Tecnico t1 = new Tecnico("T01", "Juan Perez", "Mantenimiento", "Mecánico");
        Tecnico t2 = new Tecnico("T02", "Maria Gomez", "Mantenimiento", "Electricista");
        baseDeDatos.put(t1.getIdUsuario(), t1);
        baseDeDatos.put(t2.getIdUsuario(), t2);
    }

    @Override
    public List<Tecnico> obtenerTodos() {
        return new ArrayList<>(baseDeDatos.values());
    }

    @Override
    public Optional<Tecnico> buscarPorId(String idUsuario) {
        return Optional.ofNullable(baseDeDatos.get(idUsuario));
    }

    @Override
    public void guardar(Tecnico tecnico) {
        baseDeDatos.put(tecnico.getIdUsuario(), tecnico);
    }
}