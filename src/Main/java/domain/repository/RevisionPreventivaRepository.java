package domain.repository;

import domain.model.RevisionPreventiva;

import java.util.List;
import java.util.Optional;

public interface RevisionPreventivaRepository {
    List<RevisionPreventiva> obtenerTodas();

    Optional<RevisionPreventiva> buscarPorId(String idRevision);

    RevisionPreventiva guardar(RevisionPreventiva revision);
}
