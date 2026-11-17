package infrastructure.persistence.repository;

import domain.model.Tecnico;
import domain.repository.TecnicoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TecnicoRepositoryJpaAdapter implements TecnicoRepository {
    private final TecnicoJpaRepository tecnicoJpaRepository;

    public TecnicoRepositoryJpaAdapter(TecnicoJpaRepository tecnicoJpaRepository) {
        this.tecnicoJpaRepository = tecnicoJpaRepository;
    }

    @Override
    public List<Tecnico> obtenerTodos() {
        return tecnicoJpaRepository.findAll();
    }

    @Override
    public Optional<Tecnico> buscarPorId(String idUsuario) {
        return tecnicoJpaRepository.findById(idUsuario);
    }

    @Override
    public void guardar(Tecnico tecnico) {
        tecnicoJpaRepository.save(tecnico);
    }
}
