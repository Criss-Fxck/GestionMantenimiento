package domain.model;

import domain.model.Equipo;
import domain.model.RevisionPreventiva;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import service.EmailService;
import service.RevisionService;
import service.SolicitudService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RevisionServiceTest {

    static {
        // Byte Buddy 1.14.12 no reconoce Java 27 todavía; permite su modo experimental.
        System.setProperty("net.bytebuddy.experimental", "true");
    }

    private SolicitudService solicitudServiceMock;
    private EmailService emailServiceMock;
    private RevisionService revisionService;

    @BeforeEach
    void setUp() {
        // Mockeamos los servicios externos para no enviar correos reales en las pruebas
        solicitudServiceMock = Mockito.mock(SolicitudService.class);
        emailServiceMock = Mockito.mock(EmailService.class);
        revisionService = new RevisionService(solicitudServiceMock, emailServiceMock);
    }

    // PRUEBA 1: Criterio 1 - Se dispara la alerta cuando es el día exacto
    @Test
    void procesarAlertas_DebeEnviarCorreoYCrearSolicitud_CuandoEsFechaDeAlerta() {
        Equipo equipo = new Equipo("EQ-100", "Impresora", "Piso 1");
        // Si el aviso es de 3 días, programamos el mantenimiento para dentro de 3 días (la alerta cae HOY)
        LocalDate fechaMantenimiento = LocalDate.now().plusDays(3);
        RevisionPreventiva revision = new RevisionPreventiva("REV-001", equipo, fechaMantenimiento, 3, "super@gmail.com");

        revisionService.registrarRevision(revision);

        // Ejecutamos el motor manualmente
        revisionService.procesarAlertasPreventivas();

        // Verificamos que los servicios hayan sido llamados 1 vez
        verify(emailServiceMock, times(1)).enviarCorreo(anyString(), anyString(), anyString());
        verify(solicitudServiceMock, times(1)).crearSolicitud(any(), any(), anyString());
        // Verificamos que se bloqueó la alerta
        assertTrue(revision.isAlertaGenerada());
    }

    // PRUEBA 2: Criterio 1 - No se dispara si aún faltan días para la alerta
    @Test
    void procesarAlertas_NoDebeHacerNada_CuandoAunNoEsFechaDeAlerta() {
        Equipo equipo = new Equipo("EQ-100", "Impresora", "Piso 1");
        // Programamos para dentro de 10 días, con aviso de 3 días (la alerta será en 7 días, NO hoy)
        LocalDate fechaMantenimiento = LocalDate.now().plusDays(10);
        RevisionPreventiva revision = new RevisionPreventiva("REV-002", equipo, fechaMantenimiento, 3, "super@gmail.com");

        revisionService.registrarRevision(revision);
        revisionService.procesarAlertasPreventivas();

        // Verificamos que NUNCA se llamó al envío de correo ni a la creación de solicitud
        verify(emailServiceMock, times(0)).enviarCorreo(anyString(), anyString(), anyString());
        verify(solicitudServiceMock, times(0)).crearSolicitud(any(), any(), anyString());
        assertFalse(revision.isAlertaGenerada());
    }

    // PRUEBA 3: Criterio 2 - La reprogramación reinicia el motor
    @Test
    void reprogramarRevision_DebeActualizarFechaYReiniciarAlerta_CuandoSeCambiaLaFecha() {
        Equipo equipo = new Equipo("EQ-100", "Impresora", "Piso 1");
        LocalDate fechaInicial = LocalDate.now().plusDays(1);
        RevisionPreventiva revision = new RevisionPreventiva("REV-003", equipo, fechaInicial, 2, "super@gmail.com");

        // Simulamos que la alerta ya había sonado
        revision.setAlertaGenerada(true);
        revisionService.registrarRevision(revision);

        LocalDate nuevaFecha = LocalDate.now().plusDays(20);

        // Ejecutamos la reprogramación
        revisionService.reprogramarRevision("REV-003", nuevaFecha);

        // Verificamos el Criterio 2: Fecha actualizada y el boolean reiniciado a false
        assertEquals(nuevaFecha, revision.getFechaProgramada());
        assertFalse(revision.isAlertaGenerada());
    }
}
