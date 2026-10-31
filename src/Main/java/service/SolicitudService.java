package service;

import domain.exeptions.SolicitudInvalidaException;
import domain.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitudService {
    private final List<SolicitudMantenimiento> solicitudes = new ArrayList<>();
    private final EmailService emailService;

    public SolicitudService(EmailService emailService) {
        this.emailService = emailService;
    }

    public List<SolicitudMantenimiento> obtenerTodas() {
        return solicitudes;
    }

    public SolicitudMantenimiento crearSolicitud(Equipo equipo, UsuarioSolicitante solicitante, String descripcion) {
        // Criterio 2: Validar selección de equipo activo
        if (equipo == null || equipo.getIdEquipo() == null || equipo.getIdEquipo().isBlank()) {
            throw new SolicitudInvalidaException("Debe seleccionar un equipo para formalizar la solicitud.");
        }

        // Criterio 2: Validar descripción detallada de la falla
        if (descripcion == null || descripcion.isBlank()) {
            throw new SolicitudInvalidaException("Debe registrar la descripción detallada de la falla.");
        }

        // Criterio 1: Generar número de ticket y guardar en estado "REPORTADO"
        int nuevoId = 1000 + solicitudes.size() + 1;
        SolicitudMantenimiento solicitud = new SolicitudMantenimiento(nuevoId, equipo, solicitante, descripcion);

        String cuerpo = "Se ha creado la solicitud #" + solicitud.getIdSolicitud() +
                " para el equipo " + equipo.getNombre() +
                ". Falla reportada: " + descripcion;

        emailService.enviarCorreo(
                solicitante.getEmail(),
                "Notificación Ticket #" + solicitud.getIdSolicitud(),
                cuerpo
        );

        solicitudes.add(solicitud);
        return solicitud;
    }
}