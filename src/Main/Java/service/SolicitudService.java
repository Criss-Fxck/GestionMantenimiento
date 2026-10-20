package service;

import domain.model.*;
import infrastructure.email.EmailService;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitudService {
    private final List<SolicitudMantenimiento> solicitudes = new ArrayList<>();
    private final EmailService emailService;

    public SolicitudService(EmailService emailService) {
        this.emailService = emailService;
        UsuarioSolicitante solicitante = new UsuarioSolicitante("US-01", "Ana Gómez", "Contabilidad");
        Tecnico tecnico = new Tecnico("TEC-01", "Carlos Mendoza", "Sistemas");
        Equipo equipo = new Equipo("EQ-100", "Impresora Industrial", "Piso 2");

        SolicitudMantenimiento inicial = new SolicitudMantenimiento(1001, equipo, solicitante, "Falla en rodillos de impresión");
        inicial.asignarTecnico(tecnico);
        solicitudes.add(inicial);
    }

    public List<SolicitudMantenimiento> obtenerTodas() {
        return solicitudes;
    }

    public SolicitudMantenimiento crearSolicitud(Equipo equipo, UsuarioSolicitante solicitante, String descripcion) {
        int nuevoId = 1000 + solicitudes.size() + 1;
        SolicitudMantenimiento solicitud = new SolicitudMantenimiento(nuevoId, equipo, solicitante, descripcion);
        solicitudes.add(solicitud);

        // Envío de correo electrónico
        String cuerpo = "Se ha creado la solicitud #" + solicitud.getIdSolicitud() +
                " para el equipo " + equipo.getNombre() +
                ". Falla reportada: " + descripcion;

        emailService.enviarCorreo(
                solicitante.getEmail(),
                "Notificación Ticket #" + solicitud.getIdSolicitud(),
                cuerpo
        );

        return solicitud;
    }
}
