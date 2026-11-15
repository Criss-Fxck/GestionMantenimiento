package domain.model;

import domain.exeptions.SolicitudInvalidaException;
import domain.repository.TecnicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import service.EmailService;
import service.SolicitudService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacionTest {

    @Mock
    private EmailService emailService;

    @Mock
    private TecnicoRepository tecnicoRepository;

    @InjectMocks
    private SolicitudService solicitudService;

    // PRUEBA 1: Camino feliz (Creación exitosa)
    @Test
    void deberiaCrearSolicitudExitosamente() {
        // Prepare (Given)
        Equipo equipo = new Equipo("EQ-101", "Laptop Lenovo", "Oficina 302");
        UsuarioSolicitante usuario = new UsuarioSolicitante("USR-001", "Carlos", "Sistemas", "carlos@empresa.com");
        Tecnico tecnicoMock = mock(Tecnico.class);

        when(tecnicoMock.getCasosActivos()).thenReturn(0);
        when(tecnicoMock.getNombre()).thenReturn("Pedro");
        when(tecnicoMock.getIdUsuario()).thenReturn("TEC-01");
        when(tecnicoRepository.obtenerTodos()).thenReturn(List.of(tecnicoMock));

        // Act (When)
        SolicitudMantenimiento resultado = solicitudService.crearSolicitud(equipo, usuario, "Pantalla azul");

        // Assert (Then)
        assertNotNull(resultado);
        assertEquals(1001, resultado.getIdSolicitud());
        verify(emailService, times(1)).enviarCorreo(anyString(), anyString(), anyString());
    }

    // PRUEBA 2: Validar freno por falta de Equipo (null)
    @Test
    void deberiaLanzarExcepcionSiFaltaEquipo() {
        UsuarioSolicitante usuario = new UsuarioSolicitante("USR-001", "Carlos", "Sistemas", "carlos@empresa.com");

        assertThrows(SolicitudInvalidaException.class, () -> {
            solicitudService.crearSolicitud(null, usuario, "Pantalla azul");
        });
    }

    // PRUEBA 3: Validar freno por Descripción vacía
    @Test
    void deberiaLanzarExcepcionSiDescripcionEstaVacia() {
        Equipo equipo = new Equipo("EQ-101", "Laptop Lenovo", "Oficina 302");
        UsuarioSolicitante usuario = new UsuarioSolicitante("USR-001", "Carlos", "Sistemas", "carlos@empresa.com");

        assertThrows(SolicitudInvalidaException.class, () -> {
            solicitudService.crearSolicitud(equipo, usuario, "");
        });
    }
}