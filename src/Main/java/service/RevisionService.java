package service;

import domain.model.Equipo;
import domain.model.RevisionPreventiva;
import domain.model.UsuarioSolicitante;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RevisionService {
    private final List<RevisionPreventiva> revisiones = new ArrayList<>();
    private final SolicitudService solicitudService;
    private final EmailService emailService;

    public RevisionService(SolicitudService solicitudService, EmailService emailService) {
        this.solicitudService = solicitudService;
        this.emailService = emailService;
    }

    // CRITERIO 1: Motor de alertas (Se ejecuta todos los días a las 8:00 AM)
    // @Scheduled(cron = "0 0 8 * * ?")
    @Scheduled(fixedRate = 10000)
    public void procesarAlertasPreventivas() {
        LocalDate hoy = LocalDate.now();

        for (RevisionPreventiva revision : revisiones) {
            if (!revision.isAlertaGenerada()) {
                LocalDate fechaAlerta = revision.getFechaProgramada().minusDays(revision.getDiasAvisoPrevio());

                if (hoy.isEqual(fechaAlerta) || hoy.isAfter(fechaAlerta)) {
                    // 1. Enviar notificación al supervisor
                    String asunto = "ALERTA: Mantenimiento Preventivo Próximo - " + revision.getEquipo().getNombre();
                    String cuerpo = "El equipo requiere revisión el " + revision.getFechaProgramada() + ". Se ha generado la orden previa.";
                    emailService.enviarCorreo(revision.getEmailSupervisor(), asunto, cuerpo);

                    // 2. Crear la orden previa (Solicitud)
                    UsuarioSolicitante sistema = new UsuarioSolicitante("SYS", "Sistema Motor Alertas", "Mantenimiento", revision.getEmailSupervisor());
                    solicitudService.crearSolicitud(revision.getEquipo(), sistema, "Revisión preventiva programada");

                    // 3. Marcar como generada para no repetir
                    revision.setAlertaGenerada(true);
                }
            }
        }
    }

    // CRITERIO 2: Reprogramar revisión (Recalcula la alerta)
    public void reprogramarRevision(String idRevision, LocalDate nuevaFecha) {
        Optional<RevisionPreventiva> revisionOpt = revisiones.stream()
                .filter(r -> r.getIdRevision().equals(idRevision))
                .findFirst();

        if (revisionOpt.isPresent()) {
            RevisionPreventiva revision = revisionOpt.get();
            revision.setFechaProgramada(nuevaFecha);
            // Al reiniciar este flag, el motor volverá a evaluar la nueva fecha
            revision.setAlertaGenerada(false);
        }
    }

    public void registrarRevision(RevisionPreventiva revision) {
        revisiones.add(revision);
    }
}