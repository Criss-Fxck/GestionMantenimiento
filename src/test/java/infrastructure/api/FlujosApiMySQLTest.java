package infrastructure.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.repository.RevisionPreventivaRepository;
import domain.repository.SolicitudRepository;
import domain.repository.TecnicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import service.EmailService;
import service.RevisionService;
import mantenimiento.main;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = main.class)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class FlujosApiMySQLTest {

    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RevisionService revisionService;

    @Autowired
    private RevisionPreventivaRepository revisionRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @MockBean
    private EmailService emailService;

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Test
    void integraLosFlujosPrincipalesDeLaApiConMySQL() throws Exception {
        mockMvc.perform(get("/api/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idEquipo":"EQ-FLUJO-API","marca":"Acme","modelo":"M-500","ubicacion":"Laboratorio"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEquipo").value("EQ-FLUJO-API"))
                .andExpect(jsonPath("$.marca").value("Acme"))
                .andExpect(jsonPath("$.modelo").value("M-500"))
                .andExpect(jsonPath("$.nombre").value("Acme M-500"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idEquipo":"EQ-FLUJO-API","marca":"Otra marca","modelo":"Otro modelo","ubicacion":"Otro lugar"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("Ya existe un equipo registrado con el código EQ-FLUJO-API."));

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idEquipo":"EQ-SIN-MODELO-API","marca":"Acme","modelo":"  ","ubicacion":"Laboratorio"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());

        mockMvc.perform(post("/api/equipos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idEquipo":"EQ-FUERA-API","marca":"Acme","modelo":"M-900","ubicacion":"Taller"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/equipos/EQ-FUERA-API/fuera-de-servicio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Fuera de servicio"));
        mockMvc.perform(get("/api/equipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idEquipo == 'EQ-FUERA-API')].estado")
                        .value(hasItem("Fuera de servicio")));
        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipoId":"EQ-FUERA-API","solicitanteId":"USR-EQUIPO-FUERA","solicitanteNombre":"Solicitante","departamento":"Operaciones","email":"solicitante@example.com","descripcion":"Falla de equipo inhabilitado"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/tecnicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idUsuario":"T-LIMITE-API","nombre":"Tecnico de prueba","departamento":"Mantenimiento","especialidad":"Mecánica"}
                                """))
                .andExpect(status().isOk());

        for (int caso = 0; caso < 5; caso++) {
            mockMvc.perform(post("/api/tecnicos/T-LIMITE-API/asignar"))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/api/tecnicos/T-LIMITE-API/asignar"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").exists());

        mockMvc.perform(get("/api/tecnicos/carga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idUsuario == 'T-LIMITE-API')].casosActivos").value(hasItem(5)));

        String respuestaSolicitud = mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipoId":"EQ-FLUJO-API","equipoNombre":"Ignorado","equipoUbicacion":"Ignorada","solicitanteId":"USR-FLUJO-API","solicitanteNombre":"Solicitante de prueba","departamento":"Operaciones","email":"solicitante@example.com","descripcion":"Falla de integración"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ASIGNADO"))
                .andReturn().getResponse().getContentAsString();

        JsonNode solicitudCreada = objectMapper.readTree(respuestaSolicitud);
        int idSolicitud = solicitudCreada.path("idSolicitud").asInt();
        String idTecnicoAsignado = solicitudCreada.path("tecnicoAsignado").path("idUsuario").asText();
        verify(emailService).enviarCorreo(
                eq("solicitante@example.com"), contains("asignada"), contains("ASIGNADO"));

        mockMvc.perform(get("/api/solicitudes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/api/solicitudes/{id}/cerrar", idSolicitud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diagnostico":"  Diagnóstico validado  ","solucion":"  Reparación completada  "}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Cerrado"))
                .andExpect(jsonPath("$.diagnostico").value("Diagnóstico validado"))
                .andExpect(jsonPath("$.solucion").value("Reparación completada"));

        mockMvc.perform(post("/api/solicitudes/{id}/cerrar", idSolicitud)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diagnostico":"Otra vez","solucion":"Otra vez"}
                                """))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/solicitudes/{id}/cerrar", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"diagnostico":"Diagnóstico","solucion":"Solución"}
                                """))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tecnicos/carga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.idUsuario == '" + idTecnicoAsignado + "')].casosActivos").value(hasItem(0)));

        mockMvc.perform(post("/api/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"equipoId":"EQ-FLUJO-API","solicitanteId":"USR-INVALIDA-API","solicitanteNombre":"Solicitante","departamento":"Operaciones","email":"solicitante@example.com","descripcion":"  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());

        LocalDate fechaFutura = LocalDate.now().plusDays(20);
        mockMvc.perform(post("/api/revisiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idRevision":"REV-FLUJO-API","equipoId":"EQ-FLUJO-API","fechaProgramada":"%s","diasAvisoPrevio":3,"emailSupervisor":"supervisor@example.com"}
                                """.formatted(fechaFutura)))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/revisiones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idRevision").value("REV-FLUJO-API"));

        LocalDate fechaReprogramada = fechaFutura.plusDays(5);
        mockMvc.perform(put("/api/revisiones/REV-FLUJO-API/reprogramar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nuevaFecha":"%s"}
                                """.formatted(fechaReprogramada)))
                .andExpect(status().isOk());
        org.junit.jupiter.api.Assertions.assertEquals(
                fechaReprogramada,
                revisionRepository.buscarPorId("REV-FLUJO-API").orElseThrow().getFechaProgramada());

        LocalDate fechaAlerta = LocalDate.now().plusDays(1);
        mockMvc.perform(post("/api/revisiones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"idRevision":"REV-ALERTA-API","equipoId":"EQ-FLUJO-API","fechaProgramada":"%s","diasAvisoPrevio":2,"emailSupervisor":"supervisor-alerta@example.com"}
                                """.formatted(fechaAlerta)))
                .andExpect(status().isCreated());

        int solicitudesAntesDeAlerta = solicitudRepository.obtenerTodas().size();
        revisionService.procesarAlertasPreventivas();
        assertEquals(true, revisionRepository.buscarPorId("REV-ALERTA-API").orElseThrow().isAlertaGenerada());
        assertEquals(solicitudesAntesDeAlerta + 1, solicitudRepository.obtenerTodas().size());
        revisionService.procesarAlertasPreventivas();
        assertEquals(solicitudesAntesDeAlerta + 1, solicitudRepository.obtenerTodas().size());
        verify(emailService).enviarCorreo(
                eq("supervisor-alerta@example.com"), contains("ALERTA"), contains(fechaAlerta.toString()));

        mockMvc.perform(post("/api/email/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"destinatario":"prueba@example.com","asunto":"Prueba","cuerpo":"Mensaje"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/reportes/tecnicos/semanal/enviar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"emailCoordinador":"coordinador@example.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnicos", hasSize(3)));
        verify(emailService).enviarCorreo(
                eq("coordinador@example.com"), contains("Reporte semanal"), contains("Semana:"));

        int solicitudesAntesDelErrorDeCorreo = solicitudRepository.obtenerTodas().size();
        int casosActivosAntesDelErrorDeCorreo = tecnicoRepository.obtenerTodos().stream()
                .mapToInt(tecnico -> tecnico.getCasosActivos())
                .sum();
        doThrow(new RuntimeException("Fallo simulado del correo"))
                .when(emailService).enviarCorreo(eq("fallo@example.com"), anyString(), anyString());

        assertThrows(Exception.class, () -> mockMvc.perform(post("/api/solicitudes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"equipoId":"EQ-FLUJO-API","solicitanteId":"USR-ERROR-CORREO","solicitanteNombre":"Solicitante de prueba","departamento":"Operaciones","email":"fallo@example.com","descripcion":"La falla de correo debe revertir la operación"}
                        """)));

        assertEquals(solicitudesAntesDelErrorDeCorreo, solicitudRepository.obtenerTodas().size());
        assertEquals(casosActivosAntesDelErrorDeCorreo, tecnicoRepository.obtenerTodos().stream()
                .mapToInt(tecnico -> tecnico.getCasosActivos())
                .sum());
    }
}
