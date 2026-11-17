package infrastructure.persistence.repository;

import domain.model.Equipo;
import domain.repository.EquipoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EquipoRepositoryJpaAdapter implements EquipoRepository {
    private final EquipoJpaRepository equipoJpaRepository;

    public EquipoRepositoryJpaAdapter(EquipoJpaRepository equipoJpaRepository) {
        this.equipoJpaRepository = equipoJpaRepository;
    }

    @Override
    public List<Equipo> obtenerTodos() {
        return equipoJpaRepository.findAll();
    }

    @Override
    public Optional<Equipo> buscarPorId(String idEquipo) {
        return equipoJpaRepository.findFirstByIdEquipoIgnoreCase(idEquipo.trim());
    }

    @Override
    public Equipo guardar(Equipo equipo) {
        return equipoJpaRepository.save(equipo);
    }
}
