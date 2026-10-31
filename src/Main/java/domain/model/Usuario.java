package domain.model;

public abstract class Usuario {
    protected String idUsuario;
    protected String nombre;
    protected String departamento;

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