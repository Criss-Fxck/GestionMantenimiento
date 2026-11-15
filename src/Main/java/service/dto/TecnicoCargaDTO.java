package service.dto;

import domain.model.EstadoDisponibilidad;

public class TecnicoCargaDTO {
    private String idUsuario;
    private String nombre;
    private int casosActivos;
    private EstadoDisponibilidad estado;
    private String porcentajeOcupacion;

    public TecnicoCargaDTO(String idUsuario, String nombre, int casosActivos, EstadoDisponibilidad estado) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.casosActivos = casosActivos;
        this.estado = estado;
        this.porcentajeOcupacion = (casosActivos * 20) + "%"; // 1 caso = 20% de 5
    }

    // Getters
    public String getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public int getCasosActivos() { return casosActivos; }
    public EstadoDisponibilidad getEstado() { return estado; }
    public String getPorcentajeOcupacion() { return porcentajeOcupacion; }
}