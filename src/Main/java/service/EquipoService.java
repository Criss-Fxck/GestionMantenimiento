package service;

import domain.model.Equipo;
import domain.repository.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class EquipoService {
    private final EquipoRepository equipoRepository;
    private final List<Equipo> equipos = new ArrayList<>();

    public EquipoService() {
        this.equipoRepository = null;
        // Datos de prueba iniciales
        equipos.add(new Equipo("EQ-100", "Impresora Industrial", "Piso 2"));
        equipos.add(new Equipo("EQ-200", "Servidor Principal", "Data Center"));
    }

    @Autowired
    public EquipoService(EquipoRepository equipoRepository) {
        this.equipoRepository = equipoRepository;
    }

    public List<Equipo> obtenerTodos() {
        if (equipoRepository != null) {
            List<Equipo> ordenados = new ArrayList<>(equipoRepository.obtenerTodos());
            ordenados.sort(Comparator.comparing(Equipo::isCritico).reversed());
            return ordenados;
        }
        List<Equipo> ordenados = new ArrayList<>(equipos);
        // Resalta los equipos CRÍTICOS al inicio de la lista manteniendo el orden relativo de los demás
        ordenados.sort(Comparator.comparing(Equipo::isCritico).reversed());
        return ordenados;
    }

    public Optional<Equipo> buscarPorId(String idEquipo) {
        if (idEquipo == null) {
            return Optional.empty();
        }
        if (equipoRepository != null) {
            return equipoRepository.buscarPorId(idEquipo.trim());
        }
        return equipos.stream()
                .filter(e -> e.getIdEquipo().equalsIgnoreCase(idEquipo.trim()))
                .findFirst();
    }

    public Equipo registrarEquipo(String id, String nombre, String ubicacion) {
        if (buscarPorId(id).isPresent()) {
            throw codigoEquipoExistente(id);
        }
        if (equipoRepository != null) {
            return equipoRepository.guardar(new Equipo(id, nombre, ubicacion));
        }
        Equipo equipo = new Equipo(id, nombre, ubicacion);
        equipos.add(equipo);
        return equipo;
    }

    public Equipo registrarEquipo(String id, String marca, String modelo, String ubicacion) {
        String codigoValidado = validarRequerido(id, "código");
        String marcaValidada = validarRequerido(marca, "marca");
        String modeloValidado = validarRequerido(modelo, "modelo");
        String ubicacionValidada = validarRequerido(ubicacion, "ubicación");

        if (buscarPorId(codigoValidado).isPresent()) {
            throw codigoEquipoExistente(codigoValidado);
        }

        Equipo equipo = new Equipo(codigoValidado, marcaValidada, modeloValidado, ubicacionValidada);
        if (equipoRepository != null) {
            return equipoRepository.guardar(equipo);
        }
        equipos.add(equipo);
        return equipo;
    }

    public Equipo obtenerORegistrarEquipo(String id, String nombre, String ubicacion) {
        return buscarPorId(id).orElseGet(() -> registrarEquipo(id, nombre, ubicacion));
    }

    // Permite al administrador inhabilitar el equipo
    @Transactional
    public Equipo marcarFueraDeServicio(String idEquipo) {
        Equipo equipo = (equipoRepository != null
                ? equipoRepository.buscarPorId(idEquipo)
                : equipos.stream()
                    .filter(e -> e.getIdEquipo().equals(idEquipo))
                    .findFirst())
                .orElseThrow(() -> new NoSuchElementException("Equipo no encontrado: " + idEquipo));

        equipo.marcarFueraDeServicio();
        if (equipoRepository != null) {
            equipoRepository.guardar(equipo);
        }
        return equipo;
    }

    private String validarRequerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el " + campo + " del equipo.");
        }
        return valor.trim();
    }

    private IllegalStateException codigoEquipoExistente(String idEquipo) {
        return new IllegalStateException(
                "Ya existe un equipo registrado con el código " + idEquipo + ".");
    }
}
