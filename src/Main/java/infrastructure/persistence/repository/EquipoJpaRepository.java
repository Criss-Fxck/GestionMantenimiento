package infrastructure.persistence.repository;

import domain.model.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipoJpaRepository extends JpaRepository<Equipo, String> {
    Optional<Equipo> findFirstByIdEquipoIgnoreCase(String idEquipo);
}
