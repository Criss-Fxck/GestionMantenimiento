package service;

import domain.exeptions.SolicitudInvalidaException;
import domain.model.*;
import domain.repository.EquipoRepository;
import domain.repository.SolicitanteRepository;
import domain.repository.SolicitudRepository;
import domain.repository.TecnicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SolicitudService {
    private final List<SolicitudMantenimiento> solicitudes = new ArrayList<>();
    private final EmailService emailService;
    private final TecnicoRepository tecnicoRepository;
    private final EquipoService equipoService;
    private final EquipoRepository equipoRepository;
    private final SolicitanteRepository solicitanteRepository;
    private final SolicitudRepository solicitudRepository;

    public SolicitudService(EmailService emailService, TecnicoRepository tecnicoRepository) {
        this(emailService, tecnicoRepository, null, null, null, null);
    }

    @Autowired
    public SolicitudService(
            EmailService emailService,
            TecnicoRepository tecnicoRepository,
            EquipoService equipoService,
            EquipoRepository equipoRepository,
            SolicitanteRepository solicitanteRepository,
            SolicitudRepository solicitudRepository) {
        this.emailService = emailService;
        this.tecnicoRepository = tecnicoRepository;
        this.equipoService = equipoService;
        this.equipoRepository = equipoRepository;
        this.solicitanteRepository = solicitanteRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public synchronized List<SolicitudMantenimiento> obtenerTodas() {
        if (solicitudRepository != null) {
            return List.copyOf(solicitudRepository.obtenerTodas());
        }
        return List.copyOf(solicitudes);
    }

    @Transactional
    public synchronized SolicitudMantenimiento crearSolicitud(Equipo equipo, UsuarioSolicitante solicitante, String descripcion) {
        // GUARDIÁN 1: Validación de Equipo obligatorio
        if (equipo == null || equipo.getIdEquipo() == null || equipo.getIdEquipo().isBlank()) {
            throw new SolicitudInvalidaException("Debe seleccionar un equipo válido para formalizar la solicitud.");
        }

        // Sincroniza con el equipo registrado en EquipoService para conservar historial y estado real
        Equipo equipoGestionado;
        if (equipoRepository != null) {
            equipoGestionado = equipoRepository.buscarPorId(equipo.getIdEquipo())
                    .orElseThrow(() -> new NoSuchElementException("No existe el equipo " + equipo.getIdEquipo() + "."));
        } else {
            equipoGestionado = (equipoService != null)
                    ? equipoService.obtenerORegistrarEquipo(equipo.getIdEquipo(), equipo.getNombre(), equipo.getUbicacion())
                    : equipo;
        }

        // GUARDIÁN 2: Validación de estado del equipo
        if (equipoGestionado.isFueraDeServicio()) {
            throw new SolicitudInvalidaException("No se puede crear la solicitud. El equipo " + equipoGestionado.getNombre() + " está Fuera de servicio.");
        }

        // GUARDIÁN 3: Validación de Descripción obligatoria
        if (descripcion == null || descripcion.isBlank()) {
            throw new SolicitudInvalidaException("Debe registrar la descripción detallada de la falla.");
        }

        // Búsqueda de técnico disponible
        Tecnico tecnico = tecnicoRepository.obtenerTodos().stream()
                .filter(t -> t.getCasosActivos() < 5)
                .min(Comparator.comparingInt(Tecnico::getCasosActivos))
                .orElseThrow(() -> new IllegalStateException("No hay técnicos disponibles: todos alcanzaron el máximo de 5 casos activos."));

        UsuarioSolicitante solicitanteGestionado = solicitante;
        SolicitudMantenimiento solicitud;
        if (solicitudRepository != null) {
            solicitanteGestionado = solicitanteRepository.buscarPorId(solicitante.getIdUsuario())
                    .orElseGet(() -> solicitanteRepository.guardar(solicitante));
            solicitud = solicitudRepository.guardar(
                    new SolicitudMantenimiento(equipoGestionado, solicitanteGestionado, descripcion));
        } else {
            int nuevoId = 1000 + solicitudes.size() + 1;
            solicitud = new SolicitudMantenimiento(nuevoId, equipoGestionado, solicitanteGestionado, descripcion);
        }

        String cuerpo = "La solicitud #" + solicitud.getIdSolicitud() +
                " cambió al estado ASIGNADO. Técnico: " + tecnico.getNombre() +
                " (" + tecnico.getIdUsuario() + "). Equipo: " + equipoGestionado.getNombre() +
                ". Falla reportada: " + descripcion;

        emailService.enviarCorreo(
                solicitanteGestionado.getEmail(),
                "Solicitud #" + solicitud.getIdSolicitud() + " asignada",
                cuerpo
        );

        tecnico.asignarNuevoCaso();
        solicitud.asignarTecnico(tecnico);
        tecnicoRepository.guardar(tecnico);
        if (solicitudRepository != null) {
            solicitudRepository.guardar(solicitud);
        } else {
            solicitudes.add(solicitud);
        }
        return solicitud;
    }

    @Transactional
    public synchronized SolicitudMantenimiento cerrarSolicitud(int id, String diagnostico, String solucion) {
        SolicitudMantenimiento solicitud = (solicitudRepository != null)
                ? solicitudRepository.buscarPorId(id)
                    .orElseThrow(() -> new NoSuchElementException("No existe la solicitud " + id + "."))
                : solicitudes.stream()
                    .filter(s -> s.getIdSolicitud() == id)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No existe la solicitud " + id + "."));

        if (diagnostico == null || diagnostico.isBlank() || solucion == null || solucion.isBlank()) {
            throw new IllegalArgumentException("Debe escribir el diagnóstico y la solución para cerrar la solicitud.");
        }
        if ("Cerrado".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud ya está cerrada.");
        }

        emailService.enviarCorreo(
                solicitud.getSolicitante().getEmail(),
                "Solicitud #" + id + " cerrada",
                "La solicitud #" + id + " cambió al estado Cerrado.\n" +
                        "Equipo: " + solicitud.getEquipo().getNombre() + "\n" +
                        "Diagnóstico: " + diagnostico.trim() + "\n" +
                        "Solución: " + solucion.trim()
        );
        solicitud.cerrar(diagnostico, solucion);
        if (solicitudRepository != null) {
            if (solicitud.getTecnicoAsignado() != null) {
                tecnicoRepository.guardar(solicitud.getTecnicoAsignado());
            }
            equipoRepository.guardar(solicitud.getEquipo());
            solicitudRepository.guardar(solicitud);
        }
        return solicitud;
    }
}
