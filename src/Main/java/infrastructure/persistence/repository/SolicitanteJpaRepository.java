package infrastructure.persistence.repository;

import domain.model.UsuarioSolicitante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitanteJpaRepository extends JpaRepository<UsuarioSolicitante, String> {
}
