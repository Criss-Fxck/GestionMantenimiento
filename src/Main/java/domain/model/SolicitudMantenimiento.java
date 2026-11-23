package domain.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import domain.exeptions.SolicitudInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "solicitudes")
public class SolicitudMantenimiento {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudMantenimiento.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud", nullable = false)
    private Integer idSolicitud;

    @Column(name = "descripcion_falla", columnDefinition = "TEXT", nullable = false)
    private String descripcionFalla;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;
    private static int contadorSolicitudes = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private UsuarioSolicitante solicitante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnicoAsignado;

    @Column(name = "diagnostico", columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "solucion", columnDefinition = "TEXT")
    private String solucion;

    @Column(name = "fecha_hora_creacion", nullable = false)
    private LocalDateTime fechaHoraCreacion;

    @Column(name = "fecha_hora_cierre")
    private LocalDateTime fechaHoraCierre;

    protected SolicitudMantenimiento() {
    }

    public SolicitudMantenimiento(Equipo equipo, UsuarioSolicitante solicitante, String descripcionFalla) {
        this(null, equipo, solicitante, descripcionFalla);
    }

    public SolicitudMantenimiento(int idSolicitud, Equipo equipo, UsuarioSolicitante solicitante, String descripcionFalla) {
        this(Integer.valueOf(idSolicitud), equipo, solicitante, descripcionFalla);
    }

    private SolicitudMantenimiento(Integer idSolicitud, Equipo equipo, UsuarioSolicitante solicitante, String descripcionFalla) {
        // Nueva validación de regla de negocio
        if (equipo != null && equipo.isFueraDeServicio()) {
            throw new SolicitudInvalidaException(
                    "No se puede crear la solicitud. El equipo " + equipo.getNombre() + " está Fuera de servicio."
            );
        }

        this.idSolicitud = idSolicitud;
        this.equipo = equipo;
        this.solicitante = solicitante;
        this.descripcionFalla = descripcionFalla;
        this.estado = "REPORTADO";
        this.tecnicoAsignado = null;
        this.fechaHoraCreacion = LocalDateTime.now(ZoneId.of("America/Bogota"));
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
        this.fechaHoraCierre = LocalDateTime.now(ZoneId.of("America/Bogota"));
        this.estado = "Cerrado";
        if (tecnicoAsignado != null) {
            tecnicoAsignado.cerrarCaso();
        }
        if (equipo != null) {
            equipo.registrarFallaResuelta();
        }
    }

    public void mostrarDetalle() {
        logger.info("=== SOLICITUD #{} ===", idSolicitud);
        logger.info("Estado: {}", estado);
        logger.info("Equipo: {}", (equipo != null ? equipo.getNombre() : "N/A"));
        logger.info("Solicitante: {}", (solicitante != null ? solicitante.getNombre() : "N/A"));
        logger.info("Descripción: {}", descripcionFalla);
        logger.info("Técnico Asignado: {}", (tecnicoAsignado != null ? tecnicoAsignado.getNombre() : "Sin asignar"));
        logger.info("----------------------------");
    }

    public Integer getIdSolicitud() { return idSolicitud; }
    public String getDescripcionFalla() { return descripcionFalla; }
    public String getEstado() { return estado; }
    public Equipo getEquipo() { return equipo; }
    public UsuarioSolicitante getSolicitante() { return solicitante; }
    public Tecnico getTecnicoAsignado() { return tecnicoAsignado; }
    public String getDiagnostico() { return diagnostico; }
    public String getSolucion() { return solucion; }
    public LocalDateTime getFechaHoraCreacion() { return fechaHoraCreacion; }
    public LocalDateTime getFechaHoraCierre() { return fechaHoraCierre; }
}
