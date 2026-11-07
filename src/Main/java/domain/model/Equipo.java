package domain.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Equipo {

    private static final Logger logger = LoggerFactory.getLogger(Equipo.class);

    private String idEquipo;
    private String nombre;
    private String ubicacion;

    public Equipo(String idEquipo, String nombre, String ubicacion) {
        this.idEquipo = idEquipo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    public void mostrarInformacion() {
        logger.info("=== DATOS DEL EQUIPO ===");
        logger.info("ID domain.model.Equipo: {}", idEquipo);
        logger.info("Nombre: {}", nombre);
        logger.info("Ubicación: {}", ubicacion);
    }

    public String getIdEquipo() { return idEquipo; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
}