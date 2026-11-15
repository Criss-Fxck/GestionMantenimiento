package domain.model;

import domain.exeptions.SobrecargaLaboralException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tecnico extends Usuario {

    private static final Logger logger = LoggerFactory.getLogger(Tecnico.class);

    private String especialidad;
    private int casosActivos;
    private static final int MAX_CASOS = 5;

    public Tecnico(String idUsuario, String nombre, String departamento, String especialidad) {
        super(idUsuario, nombre, departamento);
        this.especialidad = especialidad;
        this.casosActivos = 0; // Inicia con 0 casos
    }

    // Lógica de negocio: Validar y asignar caso
    public void asignarNuevoCaso() {
        if (this.casosActivos >= MAX_CASOS) {
            throw new SobrecargaLaboralException("El técnico " + this.nombre + " ya tiene el máximo de 5 casos asignados.");
        }
        this.casosActivos++;
    }

    public void cerrarCaso() {
        if (casosActivos > 0) {
            casosActivos--;
        }
    }

    // Lógica de negocio: Calcular disponibilidad dinámica
    public EstadoDisponibilidad getDisponibilidad() {
        if (casosActivos == 0) return EstadoDisponibilidad.LIBRE;
        if (casosActivos >= MAX_CASOS) return EstadoDisponibilidad.OCUPADO_AL_LIMITE;
        return EstadoDisponibilidad.PARCIALMENTE_OCUPADO;
    }

    @Override
    public void mostrarInformacion() {
        logger.info("=== TÉCNICO ===");
        logger.info("ID: {}", idUsuario);
        logger.info("Nombre: {}", nombre);
        logger.info("Especialidad: {}", especialidad);
        logger.info("Casos Activos: {}", casosActivos);
    }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public int getCasosActivos() { return casosActivos; }
}