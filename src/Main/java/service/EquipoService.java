package service;

import domain.model.Equipo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class EquipoService {
    private final List<Equipo> equipos = new ArrayList<>();

    public EquipoService() {
        // Datos de prueba iniciales
        equipos.add(new Equipo("EQ-100", "Impresora Industrial", "Piso 2"));
        equipos.add(new Equipo("EQ-200", "Servidor Principal", "Data Center"));
    }

    public List<Equipo> obtenerTodos() {
        List<Equipo> ordenados = new ArrayList<>(equipos);
        // Resalta los equipos CRÍTICOS al inicio de la lista manteniendo el orden relativo de los demás
        ordenados.sort(Comparator.comparing(Equipo::isCritico).reversed());
        return ordenados;
    }

    public Optional<Equipo> buscarPorId(String idEquipo) {
        if (idEquipo == null) {
            return Optional.empty();
        }
        return equipos.stream()
                .filter(e -> e.getIdEquipo().equalsIgnoreCase(idEquipo.trim()))
                .findFirst();
    }

    public Equipo registrarEquipo(String id, String nombre, String ubicacion) {
        Optional<Equipo> existente = buscarPorId(id);
        if (existente.isPresent()) {
            return existente.get();
        }
        Equipo equipo = new Equipo(id, nombre, ubicacion);
        equipos.add(equipo);
        return equipo;
    }

    public Equipo obtenerORegistrarEquipo(String id, String nombre, String ubicacion) {
        return buscarPorId(id).orElseGet(() -> registrarEquipo(id, nombre, ubicacion));
    }

    // Permite al administrador inhabilitar el equipo
    public Equipo marcarFueraDeServicio(String idEquipo) {
        Equipo equipo = equipos.stream()
                .filter(e -> e.getIdEquipo().equals(idEquipo))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Equipo no encontrado: " + idEquipo));

        equipo.marcarFueraDeServicio();
        return equipo;
    }
}