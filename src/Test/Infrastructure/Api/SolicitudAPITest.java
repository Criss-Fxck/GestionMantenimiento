package infrastructure.api;

import domain.exeptions.SolicitudInvalidaException;
import domain.model.Equipo;
import domain.model.SolicitudMantenimiento;
import domain.model.UsuarioSolicitante;
import infrastructure.api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import service.SolicitudService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SolicitudAPI.class)
@Import(GlobalExceptionHandler.class)
class SolicitudAPITest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SolicitudService solicitudService;

    @Test
    @DisplayName("POST /api/solicitudes debe guardar, generar ticket y retornar HTTP 201 en estado REPORTADO")
    void deberiaCrearSolicitudRetornando201() throws Exception {
        Equipo equipo = new Equipo("EQ-101", "Laptop Lenovo ThinkPad", "Oficina 302");
        UsuarioSolicitante solicitante = new UsuarioSolicitante("USR-001", "Williams Balan", "Sistemas", "destinatario@ejemplo.com");
        SolicitudMantenimiento solicitud = new SolicitudMantenimiento(1001, equipo, solicitante, "Mantenimiento preventivo");

        when(solicitudService.crearSolicitud(any(Equipo.class), any(UsuarioSolicitante.class), eq("Mantenimiento preventivo")))
                .thenReturn(solicitud);

        String jsonBody = """
                {
                  "equipoId": "EQ-101",
                  "equipoNombre": "Laptop Lenovo ThinkPad",
                  "equipoUbicacion": "Oficina 302",
                  "solicitanteId": "USR-001",
                  "solicitanteNombre": "Williams Balan",
                  "departamento": "Sistemas",
                  "email": "destinatario@ejemplo.com",
                  "descripcion": "Mantenimiento preventivo"
                }
                """;

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idSolicitud").value(1001))
                .andExpect(jsonPath("$.estado").value("REPORTADO"))
                .andExpect(jsonPath("$.descripcionFalla").value("Mantenimiento preventivo"));
    }

    @Test
    @DisplayName("POST /api/solicitudes debe retornar HTTP 400 Bad Request y advertencia al faltar la descripción")
    void deberiaRetornar400CuandoFaltaDescripcion() throws Exception {
        when(solicitudService.crearSolicitud(any(), any(), eq("")))
                .thenThrow(new SolicitudInvalidaException("Debe registrar la descripción detallada de la falla."));

        String jsonBodySinDescripcion = """
                {
                  "equipoId": "EQ-101",
                  "equipoNombre": "Laptop Lenovo ThinkPad",
                  "equipoUbicacion": "Oficina 302",
                  "solicitanteId": "USR-001",
                  "solicitanteNombre": "Williams Balan",
                  "departamento": "Sistemas",
                  "email": "destinatario@ejemplo.com",
                  "descripcion": ""
                }
                """;

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBodySinDescripcion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Advertencia de Validación"))
                .andExpect(jsonPath("$.mensaje").value("Debe registrar la descripción detallada de la falla."));
    }
}