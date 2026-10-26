package service;

import domain.model.*;
import service.EmailService; // Import correspondiente
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitudService {
    private final List<SolicitudMantenimiento> solicitudes = new ArrayList<>();

    public List<SolicitudMantenimiento> obtenerTodas() {
        return solicitudes;
    }

    public SolicitudMantenimiento crearSolicitud(Equipo equipo, UsuarioSolicitante solicitante, String descripcion) {
        int nuevoId = 1000 + solicitudes.size() + 1;
        SolicitudMantenimiento solicitud = new SolicitudMantenimiento(nuevoId, equipo, solicitante, descripcion);
        solicitudes.add(solicitud);

        String cuerpo = "Se ha creado la solicitud #" + solicitud.getIdSolicitud() +
                " para el equipo " + equipo.getNombre() +
                ". Falla reportada: " + descripcion;

        // Envío dinámico al email del solicitante
        EmailService.enviarCorreo(
                solicitante.getEmail(),
                "Notificación Ticket #" + solicitud.getIdSolicitud(),
                cuerpo
        );

        return solicitud;
    }
}