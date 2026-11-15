package domain.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Equipo {

    private static final Logger logger = LoggerFactory.getLogger(Equipo.class);

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_CRITICO = "CRÍTICO";
    public static final int UMBRAL_FALLAS_CRITICO = 3;

    private String idEquipo;
    private String nombre;
    private String ubicacion;
    private String estado;
    private int fallasReincidentes;
    private boolean destacado;

    public Equipo(String idEquipo, String nombre, String ubicacion) {
        this.idEquipo = idEquipo;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.estado = ESTADO_ACTIVO; // Inicializado por defecto
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