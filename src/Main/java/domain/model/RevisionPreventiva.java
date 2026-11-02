package domain.model;

import java.time.LocalDate;

public class RevisionPreventiva {
    private String idRevision;
    private Equipo equipo;
    private LocalDate fechaProgramada;
    private int diasAvisoPrevio;
    private String emailSupervisor;
    private boolean alertaGenerada;

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