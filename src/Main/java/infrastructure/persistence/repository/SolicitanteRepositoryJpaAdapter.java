package infrastructure.persistence.repository;

import domain.model.UsuarioSolicitante;
import domain.repository.SolicitanteRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SolicitanteRepositoryJpaAdapter implements SolicitanteRepository {
    private final SolicitanteJpaRepository solicitanteJpaRepository;

    public SolicitanteRepositoryJpaAdapter(SolicitanteJpaRepository solicitanteJpaRepository) {
        this.solicitanteJpaRepository = solicitanteJpaRepository;
    }

    @Override
    public Optional<UsuarioSolicitante> buscarPorId(String idUsuario) {
        return solicitanteJpaRepository.findById(idUsuario);
    }

    @Override
    public UsuarioSolicitante guardar(UsuarioSolicitante solicitante) {
        return solicitanteJpaRepository.save(solicitante);
    }
}
