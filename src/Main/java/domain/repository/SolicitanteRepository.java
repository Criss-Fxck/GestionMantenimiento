package domain.repository;

import domain.model.UsuarioSolicitante;

import java.util.Optional;

public interface SolicitanteRepository {
    Optional<UsuarioSolicitante> buscarPorId(String idUsuario);

    UsuarioSolicitante guardar(UsuarioSolicitante solicitante);
}
