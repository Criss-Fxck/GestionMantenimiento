package service;

import domain.model.Equipo;
import domain.model.SolicitudMantenimiento;
import domain.model.Tecnico;
import domain.model.UsuarioSolicitante;
import domain.repository.TecnicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteSemanalServiceTest {
    private SolicitudService solicitudService;
    private TecnicoRepository tecnicoRepository;
    private EmailService emailService;
    private ReporteSemanalService reporteService;

    @BeforeEach
    void setUp() {
        solicitudService = mock(SolicitudService.class);
        tecnicoRepository = mock(TecnicoRepository.class);
        emailService = mock(EmailService.class);
        reporteService = new ReporteSemanalService(solicitudService, tecnicoRepository, emailService);
    }

    @Test
    void generaResumenSemanalPorTecnicoYEstadoIncluyendoTecnicosSinSolicitudes() {
        Tecnico juan = new Tecnico("T01", "Juan Perez", "Mantenimiento", "Mecánico");
        Tecnico maria = new Tecnico("T02", "Maria Gomez", "Operaciones", "Eléctrica");
        SolicitudMantenimiento asignada = crearSolicitud(1001, juan);
        SolicitudMantenimiento cerrada = crearSolicitud(1002, juan);
        cerrada.cerrar("Diagnóstico", "Solución");
        SolicitudMantenimiento reportada = crearSolicitud(1003, juan);

        when(tecnicoRepository.obtenerTodos()).thenReturn(List.of(juan, maria));
        when(solicitudService.obtenerTodas()).thenReturn(List.of(asignada, cerrada, reportada));

        var reporte = reporteService.generarReporteSemanal();

        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        assertEquals(lunes, reporte.inicioSemana());
        assertEquals(lunes.plusDays(6), reporte.finSemana());
        assertEquals(2, reporte.tecnicos().size());

        var resumenJuan = reporte.tecnicos().get(0);
        assertEquals("T01", resumenJuan.idUsuario());
        assertEquals("Juan Perez", resumenJuan.nombre());
        assertEquals("Mantenimiento", resumenJuan.departamento());
        assertEquals("Mecánico", resumenJuan.especialidad());
        assertEquals(3, resumenJuan.totalSolicitudes());
        assertEquals(1, resumenJuan.solicitudesReportadas());
        assertEquals(1, resumenJuan.solicitudesAsignadas());
        assertEquals(1, resumenJuan.solicitudesCerradas());

        var resumenMaria = reporte.tecnicos().get(1);
        assertEquals(0, resumenMaria.totalSolicitudes());
        assertEquals(0, resumenMaria.solicitudesReportadas());
        assertEquals(0, resumenMaria.solicitudesAsignadas());
        assertEquals(0, resumenMaria.solicitudesCerradas());
    }

    @Test
    void enviaReporteAlCorreoIndicadoConElResumenDeEstados() {
        Tecnico tecnico = new Tecnico("T01", "Juan Perez", "Mantenimiento", "Mecánico");
        SolicitudMantenimiento asignada = crearSolicitud(1001, tecnico);
        when(tecnicoRepository.obtenerTodos()).thenReturn(List.of(tecnico));
        when(solicitudService.obtenerTodas()).thenReturn(List.of(asignada));

        reporteService.enviarReporteSemanal("coordinador@empresa.com");

        verify(emailService).enviarCorreo(
                eq("coordinador@empresa.com"),
                contains("Reporte semanal"),
                contains("ASIGNADO: 1")
        );
    }

    private SolicitudMantenimiento crearSolicitud(int id, Tecnico tecnico) {
        SolicitudMantenimiento solicitud = new SolicitudMantenimiento(
                id,
                new Equipo("EQ-" + id, "Equipo de prueba", "Planta"),
                new UsuarioSolicitante("USR-1", "Solicitante", "Operaciones", "solicitante@empresa.com"),
                "Falla de prueba"
        );
        solicitud.asignarTecnico(tecnico);
        return solicitud;
    }
}
