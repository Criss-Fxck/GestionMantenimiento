package service.dto;

import domain.model.EstadoDisponibilidad;

public record ReporteTecnicoSemanalDTO(
        String idUsuario,
        String nombre,
        String departamento,
        String especialidad,
        int casosActivos,
        EstadoDisponibilidad disponibilidad,
        String porcentajeOcupacion,
        int totalSolicitudes,
        int solicitudesReportadas,
        int solicitudesAsignadas,
        int solicitudesCerradas
) {}
