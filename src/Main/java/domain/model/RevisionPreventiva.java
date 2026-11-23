package domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "revisiones_preventivas")
public class RevisionPreventiva {
    @Id
    @Column(name = "id_revision", length = 30, nullable = false)
    private String idRevision;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "equipo_id", nullable = false)
    private Equipo equipo;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Column(name = "dias_aviso_previo", nullable = false)
    private int diasAvisoPrevio;

    @Column(name = "email_supervisor", length = 254, nullable = false)
    private String emailSupervisor;

    @Column(name = "alerta_generada", nullable = false)
    private boolean alertaGenerada;

    protected RevisionPreventiva() {
    }

    public RevisionPreventiva(String idRevision, Equipo equipo, LocalDate fechaProgramada, int diasAvisoPrevio, String emailSupervisor) {
        this.idRevision = idRevision;
        this.equipo = equipo;
        this.fechaProgramada = fechaProgramada;
        this.diasAvisoPrevio = diasAvisoPrevio;
        this.emailSupervisor = emailSupervisor;
        this.alertaGenerada = false;
    }

    // Getters y Setters
    public boolean isAlertaGenerada() { return alertaGenerada; }
    public void setAlertaGenerada(boolean alertaGenerada) { this.alertaGenerada = alertaGenerada; }
    public LocalDate getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDate fechaProgramada) { this.fechaProgramada = fechaProgramada; }
    public int getDiasAvisoPrevio() { return diasAvisoPrevio; }
    public Equipo getEquipo() { return equipo; }
    public String getEmailSupervisor() { return emailSupervisor; }
    public String getIdRevision() { return idRevision; } // <--- Getter agregado
}
