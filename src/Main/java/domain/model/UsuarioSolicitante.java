package domain.model;

public class UsuarioSolicitante extends Usuario {
    private String email;

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
        System.out.println("=== SOLICITANTE ===");
        System.out.println("ID: " + idUsuario);
        System.out.println("Nombre: " + nombre);
        System.out.println("Departamento: " + departamento);
        System.out.println("Email: " + email);
    }

    public String getEmail() { return email; }
}