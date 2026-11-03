package service;

import domain.model.*;
import org.springframework.stereotype.Service;
import domain.repository.TecnicoRepository;
import java.util.ArrayList;
import java.util.List;

@Service
public class SolicitudService {
    private final List<SolicitudMantenimiento> solicitudes = new ArrayList<>();
    private final EmailService emailService;
    private final TecnicoRepository tecnicoRepository;

    public SolicitudService(EmailService emailService, TecnicoRepository tecnicoRepository) {
        this.emailService = emailService;
        this.tecnicoRepository = tecnicoRepository;
    }

    public List<SolicitudMantenimiento> obtenerTodas() {
        return solicitudes;
    }

    public synchronized SolicitudMantenimiento crearSolicitud(Equipo equipo, UsuarioSolicitante solicitante, String descripcion) {
        Tecnico tecnico = tecnicoRepository.obtenerTodos().stream()
                .filter(t -> t.getCasosActivos() < 5)
                .min(java.util.Comparator.comparingInt(Tecnico::getCasosActivos))
                .orElseThrow(() -> new IllegalStateException("No hay técnicos disponibles: todos alcanzaron el máximo de 5 casos activos."));
        int nuevoId = 1000 + solicitudes.size() + 1;
        SolicitudMantenimiento solicitud = new SolicitudMantenimiento(nuevoId, equipo, solicitante, descripcion);

        String cuerpo = "La solicitud #" + solicitud.getIdSolicitud() +
                " cambió al estado ASIGNADO. Técnico: " + tecnico.getNombre() +
                " (" + tecnico.getIdUsuario() + "). Equipo: " + equipo.getNombre() +
                ". Falla reportada: " + descripcion;

        emailService.enviarCorreo(
                solicitante.getEmail(),
                "Solicitud #" + solicitud.getIdSolicitud() + " asignada",
                cuerpo
        );

        tecnico.asignarNuevoCaso();
        solicitud.asignarTecnico(tecnico);
        tecnicoRepository.guardar(tecnico);
        solicitudes.add(solicitud);
        return solicitud;
    }

    public synchronized SolicitudMantenimiento cerrarSolicitud(int id, String diagnostico, String solucion) {
        SolicitudMantenimiento solicitud = solicitudes.stream()
                .filter(s -> s.getIdSolicitud() == id)
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("No existe la solicitud " + id + "."));
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
        return solicitud;
    }
}
