package domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "equipos")
public class Equipo {

    private static final Logger logger = LoggerFactory.getLogger(Equipo.class);

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_CRITICO = "CRÍTICO";
    public static final int UMBRAL_FALLAS_CRITICO = 3;

    @Id
    @Column(name = "id_equipo", length = 30, nullable = false)
    private String idEquipo;

    @Column(name = "nombre", length = 120, nullable = false)
    private String nombre;

    @Column(name = "marca", length = 120)
    private String marca;

    @Column(name = "modelo", length = 120)
    private String modelo;

    @Column(name = "ubicacion", length = 160, nullable = false)
    private String ubicacion;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "fallas_reincidentes", nullable = false)
    private int fallasReincidentes;

    @Column(name = "destacado", nullable = false)
    private boolean destacado;

    protected Equipo() {
    }

    public Equipo(String idEquipo, String nombre, String ubicacion) {
        this.idEquipo = idEquipo;
        this.nombre = nombre;
        this.marca = null;
        this.modelo = null;
        this.ubicacion = ubicacion;
        this.estado = ESTADO_ACTIVO; // Inicializado por defecto
        this.fallasReincidentes = 0;
        this.destacado = false;
    }

    public Equipo(String idEquipo, String marca, String modelo, String ubicacion) {
        this.idEquipo = idEquipo;
        this.marca = marca;
        this.modelo = modelo;
        this.nombre = (marca + " " + modelo).trim();
        this.ubicacion = ubicacion;
        this.estado = ESTADO_ACTIVO;
        this.fallasReincidentes = 0;
        this.destacado = false;
    }

    public void marcarFueraDeServicio() {
        this.estado = "Fuera de servicio";
    }

    public boolean isFueraDeServicio() {
        return "Fuera de servicio".equalsIgnoreCase(this.estado);
    }

    public void registrarFallaResuelta() {
        this.fallasReincidentes++;
        if (this.fallasReincidentes > UMBRAL_FALLAS_CRITICO) {
            this.destacado = true;
            if (!isFueraDeServicio()) {
                this.estado = ESTADO_CRITICO;
            }
            logger.warn("ALERTA: El equipo {} ({}) superó las {} fallas reincidentes y pasó a estado CRÍTICO.",
                    nombre, idEquipo, UMBRAL_FALLAS_CRITICO);
        }
    }

    public void mostrarInformacion() {
        logger.info("=== DATOS DEL EQUIPO ===");
        logger.info("ID Equipo: {}", idEquipo);
        logger.info("Nombre: {}", nombre);
        logger.info("Ubicación: {}", ubicacion);
        logger.info("Estado: {}", estado);
        logger.info("Fallas Reincidentes: {}", fallasReincidentes);
    }

    public String getIdEquipo() { return idEquipo; }
    public String getNombre() { return nombre; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getUbicacion() { return ubicacion; }
    public String getEstado() { return estado; }
    public int getFallasReincidentes() { return fallasReincidentes; }
    public boolean isDestacado() { return destacado; }
    public boolean isCritico() { return ESTADO_CRITICO.equals(this.estado); }

    public String getAlertaVisual() {
        if (isCritico()) {
            return "⚠️ CRÍTICO - Equipo con " + fallasReincidentes + " fallas reincidentes";
        }
        return this.estado;
    }
}
