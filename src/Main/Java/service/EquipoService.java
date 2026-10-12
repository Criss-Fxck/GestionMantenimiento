package service;

import domain.model.Equipo;
import java.util.ArrayList;
import java.util.List;

public class EquipoService {
    private final List<Equipo> equipos = new ArrayList<>();

    public EquipoService() {
        // Datos de prueba iniciales
        equipos.add(new Equipo("EQ-100", "Impresora Industrial", "Piso 2"));
        equipos.add(new Equipo("EQ-200", "Servidor Principal", "Data Center"));
    }

    public List<Equipo> obtenerTodos() {
        return equipos;
    }

    public Equipo registrarEquipo(String id, String nombre, String ubicacion) {
        Equipo equipo = new Equipo(id, nombre, ubicacion);
        equipos.add(equipo);
        return equipo;
    }
}