package service;

import domain.model.Equipo;
import domain.model.RevisionPreventiva;
import domain.model.UsuarioSolicitante;
import domain.repository.EquipoRepository;
import domain.repository.RevisionPreventivaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class RevisionService {
    private final List<RevisionPreventiva> revisiones = new ArrayList<>();
    private final SolicitudService solicitudService;
    private final EmailService emailService;
    private final EquipoRepository equipoRepository;
    private final RevisionPreventivaRepository revisionRepository;

    public RevisionService(SolicitudService solicitudService, EmailService emailService) {
        this(solicitudService, emailService, null, null);
    }

    @Autowired
    public RevisionService(
            SolicitudService solicitudService,
            EmailService emailService,
            EquipoRepository equipoRepository,
            RevisionPreventivaRepository revisionRepository) {
        this.solicitudService = solicitudService;
        this.emailService = emailService;
        this.equipoRepository = equipoRepository;
        this.revisionRepository = revisionRepository;
    }

    // CRITERIO 1: Motor de alertas (Se ejecuta todos los días a las 8:00 AM)
    // @Scheduled(cron = "0 0 8 * * ?")
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void procesarAlertasPreventivas() {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        List<RevisionPreventiva> revisionesActuales = revisionRepository != null
                ? revisionRepository.obtenerTodas()
                : revisiones;

        for (RevisionPreventiva revision : revisionesActuales) {
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
                    if (revisionRepository != null) {
                        revisionRepository.guardar(revision);
                    }
                }
            }
        }
    }

    // CRITERIO 2: Reprogramar revisión (Recalcula la alerta)
    public void reprogramarRevision(String idRevision, LocalDate nuevaFecha) {
        Optional<RevisionPreventiva> revisionOpt = revisionRepository != null
                ? revisionRepository.buscarPorId(idRevision)
                : revisiones.stream()
                    .filter(r -> r.getIdRevision().equals(idRevision))
                    .findFirst();

        if (revisionOpt.isPresent()) {
            RevisionPreventiva revision = revisionOpt.get();
            revision.setFechaProgramada(nuevaFecha);
            // Al reiniciar este flag, el motor volverá a evaluar la nueva fecha
            revision.setAlertaGenerada(false);
            if (revisionRepository != null) {
                revisionRepository.guardar(revision);
            }
        }
    }

    public List<RevisionPreventiva> obtenerTodas() {
        return revisionRepository != null
                ? List.copyOf(revisionRepository.obtenerTodas())
                : List.copyOf(revisiones);
    }

    public void registrarRevision(RevisionPreventiva revision) {
        if (revisionRepository != null) {
            Equipo equipo = equipoRepository.buscarPorId(revision.getEquipo().getIdEquipo())
                    .orElseThrow(() -> new NoSuchElementException(
                            "No existe el equipo " + revision.getEquipo().getIdEquipo() + "."));
            RevisionPreventiva revisionPersistida = new RevisionPreventiva(
                    revision.getIdRevision(), equipo, revision.getFechaProgramada(),
                    revision.getDiasAvisoPrevio(), revision.getEmailSupervisor());
            revisionRepository.guardar(revisionPersistida);
        } else {
            revisiones.add(revision);
        }
    }
}
