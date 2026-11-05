package domain.model;

import domain.repository.TecnicoRepository;
import service.EmailService;
import service.SolicitudService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SolicitudServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private TecnicoRepository tecnicoRepository;

    @InjectMocks
    private SolicitudService solicitudService;

    private Tecnico crearTecnicoConCarga(String id, String nombre, String email, int casosActivos) {
        Tecnico tecnico = new Tecnico(id, nombre, email, "Mantenimiento");
        for (int i = 0; i < casosActivos; i++) {
            tecnico.asignarNuevoCaso();
        }
        return tecnico;
    }

    @Test
    @DisplayName("1. Asigna automáticamente la nueva solicitud al técnico con menor carga de trabajo (< 5 casos)")
    void deberiaAsignarTecnicoConMenorCarga() {
        // Arrange
        Equipo equipo = new Equipo("EQ-100", "Impresora Industrial", "Piso 2");
        UsuarioSolicitante solicitante = new UsuarioSolicitante("USR-001", "Carlos", "carlos@empresa.com");

        Tecnico tecnicoCargado = crearTecnicoConCarga("T01", "Juan Perez", "juan@empresa.com", 3);
        Tecnico tecnicoLibre = crearTecnicoConCarga("T02", "Maria Gomez", "maria@empresa.com", 1);

        when(tecnicoRepository.obtenerTodos()).thenReturn(List.of(tecnicoCargado, tecnicoLibre));

        // Act
        SolicitudMantenimiento resultado = solicitudService.crearSolicitud(equipo, solicitante, "Fallo en rodillo");

        // Assert
        assertNotNull(resultado);
        assertEquals("ASIGNADO", resultado.getEstado());
        assertEquals(tecnicoLibre, resultado.getTecnicoAsignado());
        assertEquals(2, tecnicoLibre.getCasosActivos());

        verify(emailService, times(1)).enviarCorreo(
                eq("carlos@empresa.com"),
                contains("asignada"),
                contains("Maria Gomez")
        );
        verify(tecnicoRepository, times(1)).guardar(tecnicoLibre);
    }

    @Test
    @DisplayName("2. Lanza IllegalStateException al intentar crear solicitud cuando todos los técnicos tienen 5 casos activos")
    void deberiaLanzarExcepcionCuandoNoHayTecnicosDisponibles() {
        // Arrange
        Equipo equipo = new Equipo("EQ-200", "Servidor Principal", "Data Center");
        UsuarioSolicitante solicitante = new UsuarioSolicitante("USR-002", "Mariana", "mariana@empresa.com");

        Tecnico tecnicoSaturado1 = crearTecnicoConCarga("T01", "Juan Perez", "juan@empresa.com", 5);
        Tecnico tecnicoSaturado2 = crearTecnicoConCarga("T02", "Maria Gomez", "maria@empresa.com", 5);

        when(tecnicoRepository.obtenerTodos()).thenReturn(List.of(tecnicoSaturado1, tecnicoSaturado2));

        // Act & Assert
        IllegalStateException excepcion = assertThrows(
                IllegalStateException.class,
                () -> solicitudService.crearSolicitud(equipo, solicitante, "Pantalla azul")
        );

        assertTrue(excepcion.getMessage().contains("No hay técnicos disponibles"));
        verifyNoInteractions(emailService);
    }

    @Test
    @DisplayName("3. Cierra la solicitud correctamente, actualiza diagnóstico/solución, fecha de cierre y libera carga del técnico")
    void deberiaCerrarSolicitudYLiberarTecnico() {
        // Arrange
        Equipo equipo = new Equipo("EQ-200", "Servidor Principal", "Data Center");
        UsuarioSolicitante solicitante = new UsuarioSolicitante("USR-003", "Luis", "luis@empresa.com");
        Tecnico tecnico = crearTecnicoConCarga("T01", "Juan Perez", "juan@empresa.com", 2);

        when(tecnicoRepository.obtenerTodos()).thenReturn(List.of(tecnico));

        SolicitudMantenimiento solicitud = solicitudService.crearSolicitud(equipo, solicitante, "Sin disco");

        // Act
        SolicitudMantenimiento solicitudCerrada = solicitudService.cerrarSolicitud(
                solicitud.getIdSolicitud(),
                "Disco duro desconectado",
                "Reconexión de cable SATA"
        );

        // Assert
        assertEquals("Cerrado", solicitudCerrada.getEstado());
        assertEquals("Disco duro desconectado", solicitudCerrada.getDiagnostico());
        assertEquals("Reconexión de cable SATA", solicitudCerrada.getSolucion());
        assertNotNull(solicitudCerrada.getFechaHoraCierre());
        assertEquals(2, tecnico.getCasosActivos());

        verify(emailService, times(1)).enviarCorreo(
                eq("luis@empresa.com"),
                contains("cerrada"),
                contains("Reconexión de cable SATA")
        );
    }
}