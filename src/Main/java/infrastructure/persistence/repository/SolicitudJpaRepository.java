package infrastructure.persistence.repository;

import domain.model.SolicitudMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudJpaRepository extends JpaRepository<SolicitudMantenimiento, Integer> {
}
