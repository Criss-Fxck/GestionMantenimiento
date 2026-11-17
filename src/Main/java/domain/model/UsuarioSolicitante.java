package domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "solicitantes")
public class UsuarioSolicitante extends Usuario {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioSolicitante.class);

    @Column(name = "email", length = 254, nullable = false)
    private String email;

    protected UsuarioSolicitante() {
        super();
    }

    public UsuarioSolicitante(String idUsuario, String nombre, String departamento) {
        super(idUsuario, nombre, departamento);
        this.email = nombre.toLowerCase().replace(" ", "") + "@empresa.com";
    }

    public UsuarioSolicitante(String idUsuario, String nombre, String departamento, String email) {
        super(idUsuario, nombre, departamento);
        this.email = email;
    }

    @Override
    public void mostrarInformacion() {
        logger.info("=== SOLICITANTE ===");
        logger.info("ID: {}", idUsuario);
        logger.info("Nombre: {}", nombre);
        logger.info("Departamento: {}", departamento);
        logger.info("Email: {}", email);
    }

    public String getEmail() { return email; }
}
