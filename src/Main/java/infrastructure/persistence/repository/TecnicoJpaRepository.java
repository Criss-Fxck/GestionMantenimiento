package infrastructure.persistence.repository;

import domain.model.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TecnicoJpaRepository extends JpaRepository<Tecnico, String> {
}
