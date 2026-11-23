package service;

import domain.model.SolicitudMantenimiento;
import domain.model.Tecnico;
import domain.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import service.dto.ReporteSemanalDTO;
import service.dto.ReporteTecnicoSemanalDTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteSemanalService {
    private static final int MAX_CASOS_TECNICO = 5;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final SolicitudService solicitudService;
    private final TecnicoRepository tecnicoRepository;
    private final EmailService emailService;

    public ReporteSemanalService(
            SolicitudService solicitudService,
            TecnicoRepository tecnicoRepository,
            EmailService emailService) {
        this.solicitudService = solicitudService;
        this.tecnicoRepository = tecnicoRepository;
        this.emailService = emailService;
    }

    public ReporteSemanalDTO generarReporteSemanal() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = inicioSemana.plusDays(6);

        List<SolicitudMantenimiento> solicitudesSemana = solicitudService.obtenerTodas().stream()
                .filter(s -> s.getFechaHoraCreacion() != null)
                .filter(s -> {
                    LocalDate fechaCreacion = s.getFechaHoraCreacion().toLocalDate();
                    return !fechaCreacion.isBefore(inicioSemana) && !fechaCreacion.isAfter(finSemana);
                })
                .toList();

        List<ReporteTecnicoSemanalDTO> datosTecnicos = tecnicoRepository.obtenerTodos().stream()
                .map(tecnico -> crearResumenTecnico(tecnico, solicitudesSemana))
                .toList();

        return new ReporteSemanalDTO(inicioSemana, finSemana, datosTecnicos);
    }

    public ReporteSemanalDTO enviarReporteSemanal(String emailCoordinador) {
        if (emailCoordinador == null || emailCoordinador.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el correo del coordinador de operaciones.");
        }

        ReporteSemanalDTO reporte = generarReporteSemanal();
        emailService.enviarCorreo(
                emailCoordinador,
                "Reporte semanal de solicitudes de mantenimiento",
                construirCuerpoCorreo(reporte)
        );
        return reporte;
    }

    private ReporteTecnicoSemanalDTO crearResumenTecnico(
            Tecnico tecnico,
            List<SolicitudMantenimiento> solicitudesSemana) {
        List<SolicitudMantenimiento> solicitudesDelTecnico = solicitudesSemana.stream()
                .filter(s -> s.getTecnicoAsignado() != null)
                .filter(s -> tecnico.getIdUsuario().equals(s.getTecnicoAsignado().getIdUsuario()))
                .toList();

        int reportadas = contarEstado(solicitudesDelTecnico, "REPORTADO");
        int asignadas = contarEstado(solicitudesDelTecnico, "ASIGNADO");
        int cerradas = contarEstado(solicitudesDelTecnico, "Cerrado");
        int ocupacion = tecnico.getCasosActivos() * 100 / MAX_CASOS_TECNICO;

        return new ReporteTecnicoSemanalDTO(
                tecnico.getIdUsuario(),
                tecnico.getNombre(),
                tecnico.getDepartamento(),
                tecnico.getEspecialidad(),
                tecnico.getCasosActivos(),
                tecnico.getDisponibilidad(),
                ocupacion + "%",
                solicitudesDelTecnico.size(),
                reportadas,
                asignadas,
                cerradas
        );
    }

    private int contarEstado(List<SolicitudMantenimiento> solicitudes, String estado) {
        return (int) solicitudes.stream().filter(s -> estado.equals(s.getEstado())).count();
    }

    private String construirCuerpoCorreo(ReporteSemanalDTO reporte) {
        StringBuilder cuerpo = new StringBuilder();
        cuerpo.append("Reporte semanal de solicitudes de mantenimiento\n")
                .append("Semana: ")
                .append(reporte.inicioSemana().format(FORMATO_FECHA))
                .append(" al ")
                .append(reporte.finSemana().format(FORMATO_FECHA))
                .append(" (lunes a domingo)\n\n");

        if (reporte.tecnicos().isEmpty()) {
            cuerpo.append("No hay técnicos registrados.\n");
            return cuerpo.toString();
        }

        for (ReporteTecnicoSemanalDTO tecnico : reporte.tecnicos()) {
            cuerpo.append("Técnico: ").append(tecnico.nombre()).append("\n")
                    .append("ID: ").append(tecnico.idUsuario()).append("\n")
                    .append("Departamento: ").append(tecnico.departamento()).append("\n")
                    .append("Especialidad: ").append(tecnico.especialidad()).append("\n")
                    .append("Carga actual: ").append(tecnico.casosActivos()).append(" casos (")
                    .append(tecnico.porcentajeOcupacion()).append(", ")
                    .append(tecnico.disponibilidad()).append(")\n")
                    .append("Solicitudes de la semana: ").append(tecnico.totalSolicitudes()).append("\n")
                    .append("  REPORTADO: ").append(tecnico.solicitudesReportadas()).append("\n")
                    .append("  ASIGNADO: ").append(tecnico.solicitudesAsignadas()).append("\n")
                    .append("  Cerrado: ").append(tecnico.solicitudesCerradas()).append("\n\n");
        }
        return cuerpo.toString();
    }
}
