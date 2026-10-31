package service;

import domain.exeptions.SolicitudInvalidaException;
import domain.model.Equipo;
import domain.model.SolicitudMantenimiento;
import domain.model.UsuarioSolicitante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SolicitudService solicitudService;

    private Equipo equipoValido;
    private UsuarioSolicitante solicitanteValido;

    @BeforeEach
    void setUp() {
        equipoValido = new Equipo("EQ-101", "Laptop Lenovo ThinkPad", "Oficina 302");
        solicitanteValido = new UsuarioSolicitante("USR-001", "Williams Balan", "Sistemas", "williams@empresa.com");
    }

    @Test
    @DisplayName("Criterio 1: Debe generar ticket y guardar en estado REPORTADO cuando equipo y falla están completos")
    void deberiaCrearSolicitudExitosamente() {
        String descripcion = "Pantalla azul recurrente al encender el equipo";
        doNothing().when(emailService).enviarCorreo(anyString(), anyString(), anyString());

        SolicitudMantenimiento resultado = solicitudService.crearSolicitud(equipoValido, solicitanteValido, descripcion);

        assertNotNull(resultado);
        assertTrue(resultado.getIdSolicitud() > 0);
        assertEquals("REPORTADO", resultado.getEstado());
        assertEquals(descripcion, resultado.getDescripcionFalla());
        assertEquals("EQ-101", resultado.getEquipo().getIdEquipo());
        assertEquals(1, solicitudService.obtenerTodas().size());
        verify(emailService, times(1)).enviarCorreo(eq("williams@empresa.com"), anyString(), anyString());
    }

    @Test
    @DisplayName("Criterio 2: Debe lanzar SolicitudInvalidaException y no guardar cuando falta el equipo")
    void deberiaLanzarExcepcionCuandoFaltaEquipo() {
        SolicitudInvalidaException exception = assertThrows(
                SolicitudInvalidaException.class,
                () -> solicitudService.crearSolicitud(null, solicitanteValido, "Falla de disco duro")
        );

        assertEquals("Debe seleccionar un equipo para formalizar la solicitud.", exception.getMessage());
        assertEquals(0, solicitudService.obtenerTodas().size());
        verify(emailService, never()).enviarCorreo(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Criterio 2: Debe lanzar SolicitudInvalidaException cuando el ID del equipo está en blanco")
    void deberiaLanzarExcepcionCuandoIdEquipoEstaEnBlanco() {
        Equipo equipoSinId = new Equipo("", "Impresora", "Piso 1");

        SolicitudInvalidaException exception = assertThrows(
                SolicitudInvalidaException.class,
                () -> solicitudService.crearSolicitud(equipoSinId, solicitanteValido, "Falla de disco duro")
        );

        assertEquals("Debe seleccionar un equipo para formalizar la solicitud.", exception.getMessage());
        assertEquals(0, solicitudService.obtenerTodas().size());
        verify(emailService, never()).enviarCorreo(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Criterio 2: Debe lanzar SolicitudInvalidaException y no guardar cuando falta la descripción de la falla")
    void deberiaLanzarExcepcionCuandoFaltaDescripcion() {
        SolicitudInvalidaException exceptionNull = assertThrows(
                SolicitudInvalidaException.class,
                () -> solicitudService.crearSolicitud(equipoValido, solicitanteValido, null)
        );
        assertEquals("Debe registrar la descripción detallada de la falla.", exceptionNull.getMessage());

        SolicitudInvalidaException exceptionBlank = assertThrows(
                SolicitudInvalidaException.class,
                () -> solicitudService.crearSolicitud(equipoValido, solicitanteValido, "   ")
        );
        assertEquals("Debe registrar la descripción detallada de la falla.", exceptionBlank.getMessage());

        assertEquals(0, solicitudService.obtenerTodas().size());
        verify(emailService, never()).enviarCorreo(anyString(), anyString(), anyString());
    }
}