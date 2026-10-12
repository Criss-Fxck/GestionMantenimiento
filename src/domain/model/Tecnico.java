package domain.model;

public class Tecnico extends Usuario {
    private String especialidad;

    public Tecnico(String idUsuario, String nombre, String departamento) {
        super(idUsuario, nombre, departamento);
        this.especialidad = departamento;
    }

    public Tecnico(String idUsuario, String nombre, String departamento, String especialidad) {
        super(idUsuario, nombre, departamento);
        this.especialidad = especialidad;
    }

    @Override
    public void mostrarInformacion() {
        System.out.println("=== TÉCNICO ===");
        System.out.println("ID: " + idUsuario);
        System.out.println("Nombre: " + nombre);
        System.out.println("Especialidad: " + especialidad);
    }

    public String getEspecialidad() { return especialidad; }
}
