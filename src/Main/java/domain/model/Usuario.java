package domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Usuario {
    @Id
    @Column(name = "id_usuario", length = 30, nullable = false)
    protected String idUsuario;

    @Column(name = "nombre", length = 120, nullable = false)
    protected String nombre;

    @Column(name = "departamento", length = 120, nullable = false)
    protected String departamento;

    protected Usuario() {
    }

    public Usuario(String idUsuario, String nombre, String departamento) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.departamento = departamento;
    }

    public abstract void mostrarInformacion();

    public String getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getDepartamento() { return departamento; }
}
