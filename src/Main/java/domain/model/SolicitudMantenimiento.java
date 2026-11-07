package domain.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class SolicitudMantenimiento {

    // 1. Declaración del Logger
    private static final Logger logger = LoggerFactory.getLogger(SolicitudMantenimiento.class);

    private int idSolicitud;
    private String descripcionFalla;
    private String estado;
    private static int contadorSolicitudes = 0;

    private Equipo equipo;
    private UsuarioSolicitante solicitante;
    private Tecnico tecnicoAsignado;
    private String diagnostico;
    private String solucion;
    private LocalDateTime fechaHoraCierre;

    public SolicitudMantenimiento(int idSolicitud, Equipo equipo, UsuarioSolicitante solicitante, String descripcionFalla) {
        this.idSolicitud = idSolicitud;
        this.equipo = equipo;
        this.solicitante = solicitante;
        this.descripcionFalla = descripcionFalla;
        this.estado = "REPORTADO";
        this.tecnicoAsignado = null;
        contadorSolicitudes++;
    }

    public static int getContadorSolicitudes() {
        return contadorSolicitudes;
    }

    public void asignarTecnico(Tecnico tecnico) {
        this.tecnicoAsignado = tecnico;
        this.estado = "ASIGNADO";
    }

    public void cerrar(String diagnostico, String solucion) {
        if (diagnostico == null || diagnostico.isBlank() || solucion == null || solucion.isBlank()) {
            throw new IllegalArgumentException("Debe escribir el diagnóstico y la solución para cerrar la solicitud.");
        }
        if ("Cerrado".equals(estado)) {
            throw new IllegalStateException("La solicitud ya está cerrada.");
        }
        this.diagnostico = diagnostico.trim();
        this.solucion = solucion.trim();
        this.fechaHoraCierre = LocalDateTime.now();
        this.estado = "Cerrado";
        if (tecnicoAsignado != null) tecnicoAsignado.cerrarCaso();
    }

    public void mostrarDetalle() {
        // 2. Uso de logger.info reemplazando las concatenaciones (+) por llaves ({})
        logger.info("=== SOLICITUD #{} ===", idSolicitud);
        logger.info("Estado: {}", estado);
        logger.info("domain.model.Equipo: {}", (equipo != null ? equipo.getNombre() : "N/A"));
        logger.info("Solicitante: {}", (solicitante != null ? solicitante.getNombre() : "N/A"));
        logger.info("Descripción: {}", descripcionFalla);
        logger.info("Técnico Asignado: {}", (tecnicoAsignado != null ? tecnicoAsignado.getNombre() : "Sin asignar"));
        logger.info("----------------------------");
    }

    public int getIdSolicitud() { return idSolicitud; }
    public String getDescripcionFalla() { return descripcionFalla; }
    public String getEstado() { return estado; }
    public Equipo getEquipo() { return equipo; }
    public UsuarioSolicitante getSolicitante() { return solicitante; }
    public Tecnico getTecnicoAsignado() { return tecnicoAsignado; }
    public String getDiagnostico() { return diagnostico; }
    public String getSolucion() { return solucion; }
    public LocalDateTime getFechaHoraCierre() { return fechaHoraCierre; }
}