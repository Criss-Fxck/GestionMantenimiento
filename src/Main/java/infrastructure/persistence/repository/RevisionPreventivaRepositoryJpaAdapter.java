package infrastructure.persistence.repository;

import domain.model.RevisionPreventiva;
import domain.repository.RevisionPreventivaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RevisionPreventivaRepositoryJpaAdapter implements RevisionPreventivaRepository {
    private final RevisionPreventivaJpaRepository revisionJpaRepository;

    public RevisionPreventivaRepositoryJpaAdapter(RevisionPreventivaJpaRepository revisionJpaRepository) {
        this.revisionJpaRepository = revisionJpaRepository;
    }

    @Override
    public List<RevisionPreventiva> obtenerTodas() {
        return revisionJpaRepository.findAll();
    }

    @Override
    public Optional<RevisionPreventiva> buscarPorId(String idRevision) {
        return revisionJpaRepository.findById(idRevision);
    }

    @Override
    public RevisionPreventiva guardar(RevisionPreventiva revision) {
        return revisionJpaRepository.save(revision);
    }
}
