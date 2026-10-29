package domain.model;

public class Equipo {
    private String idEquipo;
    private String nombre;
    private String ubicacion;

    public Equipo(String idEquipo, String nombre, String ubicacion) {
        this.idEquipo = idEquipo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    public void mostrarInformacion() {
        System.out.println("=== DATOS DEL EQUIPO ===");
        System.out.println("ID domain.model.Equipo: " + idEquipo);
        System.out.println("Nombre: " + nombre);
        System.out.println("Ubicación: " + ubicacion);
    }

    public String getIdEquipo() { return idEquipo; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
}

