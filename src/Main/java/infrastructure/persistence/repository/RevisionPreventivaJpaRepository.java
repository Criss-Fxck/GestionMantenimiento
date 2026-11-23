package infrastructure.persistence.repository;

import domain.model.RevisionPreventiva;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevisionPreventivaJpaRepository extends JpaRepository<RevisionPreventiva, String> {
}
